package web.car_system.Car_Service.service.advisor.impl;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.config.RagProperties;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarEmbedding;
import web.car_system.Car_Service.repository.CarAttributeRepository;
import web.car_system.Car_Service.repository.CarEmbeddingRepository;
import web.car_system.Car_Service.service.advisor.VectorSearchService;
import web.car_system.Car_Service.service.advisor.util.CosineSimilarity;
import web.car_system.Car_Service.service.advisor.util.VectorJsonCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * In-memory cosine search với hybrid filter (price + seats).
 *
 * Tradeoff đã chọn (xem file plan):
 *  - 1000 xe × 384 dim ≈ 1.5 MB RAM, cosine chạy <5ms → đủ nhanh cho scale showroom.
 *  - Nếu vượt 100K xe → migrate sang pgvector/Qdrant (chỉ thay impl, giữ interface).
 *
 * Thread safety:
 *  - `cache` được swap nguyên (volatile) khi reload — readers thấy old hoặc new map,
 *    không bao giờ thấy map nửa-vời.
 */
@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchServiceImpl.class);

    private final CarEmbeddingRepository repo;
    private final CarAttributeRepository attrRepo;
    private final RagProperties props;

    private volatile Map<Integer, CarVecInfo> cache = Collections.emptyMap();

    public VectorSearchServiceImpl(CarEmbeddingRepository repo,
                                    CarAttributeRepository attrRepo,
                                    RagProperties props) {
        this.repo = repo;
        this.attrRepo = attrRepo;
        this.props = props;
    }

    @PostConstruct
    public void init() {
        if (!props.isEnabled()) {
            log.info("RAG disabled (ai.rag.enabled=false). Skipping embedding cache load.");
            return;
        }
        try {
            reloadCache();
        } catch (Exception ex) {
            // Không để app fail startup nếu DB chưa có bảng / chưa có data
            log.warn("Could not load embedding cache at startup: {}", ex.getMessage());
        }
    }

    @Override
    public void reloadCache() {
        String modelVersion = props.getEmbedding().getModel();
        List<CarEmbedding> all = repo.findAllByModelVersion(modelVersion);

        // Pre-compute pure-EV set một lần (1 query, ~58 IDs cho catalog hiện tại).
        Set<Integer> evIds;
        try {
            evIds = new HashSet<>(attrRepo.findPureEvCarIds());
        } catch (Exception ex) {
            log.warn("Could not load EV car IDs (filter EV sẽ vô hiệu): {}", ex.getMessage());
            evIds = Collections.emptySet();
        }

        Map<Integer, CarVecInfo> next = new HashMap<>(Math.max(16, all.size() * 2));
        for (CarEmbedding e : all) {
            Car car = e.getCar();
            if (car == null) continue;
            try {
                float[] vec = VectorJsonCodec.decode(e.getEmbeddingJson());
                boolean isEv = evIds.contains(car.getCarId());
                next.put(car.getCarId(),
                        new CarVecInfo(car.getCarId(), vec, car.getPrice(), car.getSeats(), isEv));
            } catch (Exception ex) {
                log.warn("Skip corrupted embedding for car {}: {}",
                        car.getCarId(), ex.getMessage());
            }
        }
        this.cache = Collections.unmodifiableMap(next);
        log.info("Loaded {} car embeddings into memory (model={}, EV count={})",
                next.size(), modelVersion, evIds.size());
    }

    @Override
    public List<ScoredCar> findTopK(float[] queryVector, int k) {
        return findTopK(queryVector, k, info -> true);
    }

    @Override
    public List<ScoredCar> findTopK(float[] queryVector, int k, Predicate<CarVecInfo> filter) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("queryVector must not be empty");
        }
        if (filter == null) filter = info -> true;

        Map<Integer, CarVecInfo> snapshot = this.cache;
        if (snapshot.isEmpty()) {
            log.warn("Embedding cache is empty — call reloadCache() or run scheduler first.");
            return List.of();
        }

        double minScore = props.getRetrieval().getMinScore();

        List<ScoredCar> scored = new ArrayList<>(snapshot.size());
        double maxScoreSeen = Double.NEGATIVE_INFINITY;
        Integer maxScoreCarId = null;
        int dimMismatchCount = 0;
        int filtered = 0;
        int totalConsidered = 0;

        for (CarVecInfo info : snapshot.values()) {
            if (!filter.test(info)) {
                filtered++;
                continue;
            }
            totalConsidered++;
            try {
                double score = CosineSimilarity.compute(queryVector, info.vector());
                if (score > maxScoreSeen) {
                    maxScoreSeen = score;
                    maxScoreCarId = info.carId();
                }
                if (score >= minScore) {
                    scored.add(new ScoredCar(info.carId(), score));
                }
            } catch (IllegalArgumentException dimMismatch) {
                dimMismatchCount++;
            }
        }

        if (dimMismatchCount > 0) {
            log.warn("findTopK: {} cache entries had dimension mismatch with query (qDim={}). " +
                    "Cần regenerate sau khi đổi model.", dimMismatchCount, queryVector.length);
        }

        scored.sort((x, y) -> Double.compare(y.score(), x.score()));

        if (log.isInfoEnabled()) {
            log.info("findTopK: filteredOut={}, considered={}, ≥minScore={}, returning={} (top score={})",
                    filtered, totalConsidered, scored.size(),
                    Math.min(scored.size(), k),
                    scored.isEmpty() ? "n/a" : String.format("%.4f", scored.get(0).score()));
        }

        if (scored.isEmpty() && totalConsidered > 0) {
            log.warn("findTopK: NO car ≥ minScore={}. maxScoreInCache={} (carId={}). " +
                    "→ Nếu filter price/seats khắt khe, thử nới lỏng; hoặc hạ min-score xuống dưới {}.",
                    minScore,
                    maxScoreSeen == Double.NEGATIVE_INFINITY ? "n/a" : String.format("%.4f", maxScoreSeen),
                    maxScoreCarId,
                    maxScoreSeen == Double.NEGATIVE_INFINITY ? "n/a" : String.format("%.2f", maxScoreSeen));
        }

        return scored.size() > k ? scored.subList(0, k) : scored;
    }

    @Override
    public int cacheSize() {
        return cache.size();
    }
}
