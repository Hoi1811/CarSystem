package web.car_system.Car_Service.service.advisor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.config.RagProperties;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarAttribute;
import web.car_system.Car_Service.domain.entity.CarEmbedding;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.repository.CarAttributeRepository;
import web.car_system.Car_Service.repository.CarEmbeddingRepository;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.service.advisor.EmbeddingPipelineService;
import web.car_system.Car_Service.service.advisor.EmbeddingService;
import web.car_system.Car_Service.service.advisor.VectorSearchService;
import web.car_system.Car_Service.service.advisor.util.CarTextBuilder;
import web.car_system.Car_Service.service.advisor.util.VectorJsonCodec;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Pipeline orchestrator — đọc Car + CarAttribute, build text, gọi embed, lưu DB,
 * cuối cùng reload cache để search service thấy data mới.
 *
 * QUAN TRỌNG về transaction:
 *  - regenerateAll() KHÔNG có @Transactional (chạy ngoài tx).
 *  - upsertEmbedding(carId, ...) có @Transactional(REQUIRES_NEW) — mỗi xe
 *    commit độc lập, fail xe này KHÔNG rollback xe khác. Re-fetch Car bằng
 *    findById bên trong tx để các quan hệ lazy (manufacturer, segment) init OK.
 *  - Để Spring AOP work, gọi qua self-proxy (lookup ApplicationContext) — gọi
 *    `this.upsertEmbedding(...)` từ cùng class sẽ bypass proxy & mất transaction.
 */
@Service
public class EmbeddingPipelineServiceImpl implements EmbeddingPipelineService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingPipelineServiceImpl.class);

    private final CarRepository carRepository;
    private final CarAttributeRepository carAttributeRepository;
    private final CarEmbeddingRepository embeddingRepository;
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final RagProperties props;
    private final ApplicationContext appContext;

    public EmbeddingPipelineServiceImpl(CarRepository carRepository,
                                        CarAttributeRepository carAttributeRepository,
                                        CarEmbeddingRepository embeddingRepository,
                                        EmbeddingService embeddingService,
                                        VectorSearchService vectorSearchService,
                                        RagProperties props,
                                        ApplicationContext appContext) {
        this.carRepository = carRepository;
        this.carAttributeRepository = carAttributeRepository;
        this.embeddingRepository = embeddingRepository;
        this.embeddingService = embeddingService;
        this.vectorSearchService = vectorSearchService;
        this.props = props;
        this.appContext = appContext;
    }

    @Override
    public EmbedStats regenerateAll() {
        return regenerateAll(Integer.MAX_VALUE);
    }

    @Override
    public EmbedStats regenerateAll(int limit) {
        long start = System.currentTimeMillis();
        String modelVersion = props.getEmbedding().getModel();
        long sleepMs = props.getEmbedding().getBatchSleepMs();

        // Chỉ lấy car_id + name để pipeline iterate. KHÔNG truy cập manufacturer/segment
        // ở đây vì entity sẽ detach ngay khi findAll() return — lazy proxy fail.
        List<Car> allCars = carRepository.findAll();
        List<Car> cars = limit < allCars.size() ? allCars.subList(0, limit) : allCars;
        Map<Integer, List<CarAttribute>> attrByCar = loadAttributesGroupedByCar();

        // Lấy proxy bean để @Transactional(REQUIRES_NEW) có hiệu lực
        EmbeddingPipelineServiceImpl self = appContext.getBean(EmbeddingPipelineServiceImpl.class);

        int created = 0, updated = 0, skipped = 0, failed = 0;
        log.info("Embedding pipeline START: {} cars (limit={}), model={}",
                cars.size(), limit == Integer.MAX_VALUE ? "ALL" : limit, modelVersion);

        for (Car car : cars) {
            Integer carId = car.getCarId();
            String carName = car.getName(); // field thường, OK trên detached
            try {
                Result r = self.upsertEmbedding(carId,
                        attrByCar.getOrDefault(carId, List.of()),
                        modelVersion);
                switch (r) {
                    case CREATED -> created++;
                    case UPDATED -> updated++;
                    case SKIPPED -> skipped++;
                }
                if (sleepMs > 0 && r != Result.SKIPPED) {
                    Thread.sleep(sleepMs);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Embedding pipeline interrupted at car {}", carId);
                break;
            } catch (Exception ex) {
                failed++;
                log.error("Failed to embed car {} ({}): {}", carId, carName, ex.getMessage());
            }
        }

        // Reload cache để search service thấy dữ liệu mới
        try {
            vectorSearchService.reloadCache();
        } catch (Exception ex) {
            log.warn("Reload cache failed: {}", ex.getMessage());
        }

        long elapsed = System.currentTimeMillis() - start;
        EmbedStats stats = new EmbedStats(cars.size(), created, updated, skipped, failed, elapsed);
        log.info("Embedding pipeline DONE: {}", stats);
        return stats;
    }

    @Override
    public void embedSingleCar(Integer carId) {
        // Không cần @Transactional ở đây vì delegate đã có REQUIRES_NEW.
        EmbeddingPipelineServiceImpl self = appContext.getBean(EmbeddingPipelineServiceImpl.class);
        List<CarAttribute> attrs = self.loadAttributesForCar(carId);
        self.upsertEmbedding(carId, attrs, props.getEmbedding().getModel());
        vectorSearchService.reloadCache();
    }

    // ==================== Internals ====================

    public enum Result { CREATED, UPDATED, SKIPPED }

    /**
     * Load attribute của 1 xe trong tx riêng (read-only) — caller `embedSingleCar`
     * dùng để chuẩn bị data trước khi vào tx ghi.
     */
    @Transactional(readOnly = true)
    public List<CarAttribute> loadAttributesForCar(Integer carId) {
        return carAttributeRepository.findByCarCarId(carId);
    }

    /**
     * REQUIRES_NEW: mỗi xe commit/rollback độc lập — fail giữa chừng không phá xe đã embed.
     *
     * QUAN TRỌNG: nhận `carId` thay vì Car entity, rồi findById() bên trong tx này.
     * Lý do: Car từ caller có thể đang detached → access manufacturer/segment lazy
     * sẽ throw "Could not initialize proxy - no session". Re-fetch tươi giải quyết.
     *
     * Public để Spring proxy intercept (private method bị bypass AOP).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result upsertEmbedding(Integer carId, List<CarAttribute> attrs, String modelVersion) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new BusinessException("Car not found: " + carId));

        String sourceText = CarTextBuilder.build(car, attrs);
        String hash = CarTextBuilder.buildSourceTextHash(sourceText);

        Optional<CarEmbedding> existing =
                embeddingRepository.findByCar_CarIdAndModelVersion(carId, modelVersion);

        if (existing.isPresent() && hash.equals(existing.get().getSourceTextHash())) {
            return Result.SKIPPED;
        }

        float[] vector = embeddingService.embedDocument(sourceText);
        String vectorJson = VectorJsonCodec.encode(vector);

        if (existing.isPresent()) {
            CarEmbedding e = existing.get();
            e.setEmbeddingJson(vectorJson);
            e.setSourceText(sourceText);
            e.setSourceTextHash(hash);
            e.setDimensions(vector.length);
            e.setEmbeddedAt(LocalDateTime.now());
            embeddingRepository.save(e);
            return Result.UPDATED;
        }

        CarEmbedding fresh = CarEmbedding.builder()
                .car(car)
                .embeddingJson(vectorJson)
                .sourceText(sourceText)
                .sourceTextHash(hash)
                .modelVersion(modelVersion)
                .dimensions(vector.length)
                .embeddedAt(LocalDateTime.now())
                .build();
        embeddingRepository.save(fresh);
        return Result.CREATED;
    }

    private Map<Integer, List<CarAttribute>> loadAttributesGroupedByCar() {
        List<CarAttribute> all = carAttributeRepository.findAllFetchAttribute();
        Map<Integer, List<CarAttribute>> map = new HashMap<>(Math.max(16, all.size() / 90));
        for (CarAttribute ca : all) {
            if (ca.getCar() == null) continue;
            map.computeIfAbsent(ca.getCar().getCarId(), k -> new ArrayList<>()).add(ca);
        }
        return map;
    }
}
