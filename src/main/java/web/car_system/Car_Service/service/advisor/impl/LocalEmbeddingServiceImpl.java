package web.car_system.Car_Service.service.advisor.impl;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.config.RagProperties;
import web.car_system.Car_Service.domain.dto.advisor.EmbeddingTaskType;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.service.advisor.EmbeddingService;

import java.io.IOException;
import java.time.Duration;

/**
 * Embedding bằng model SBERT chạy local trong JVM thông qua DJL (Deep Java Library).
 *
 * Ưu điểm so với Gemini API:
 *  - KHÔNG có rate limit (5 RPM / 100 RPD của Gemini free tier).
 *  - Chạy offline sau lần download đầu (~600MB cache trong %USERPROFILE%/.djl.ai).
 *  - Deterministic (cùng input → cùng vector), tốt cho hash-skip.
 *
 * Lifecycle:
 *  - Lazy init: model chỉ load khi gọi embed() lần đầu → BE startup nhanh.
 *  - First call ~5-10s (load model vào RAM), subsequent calls ~50ms/text.
 *  - @PreDestroy: đóng predictor + model để giải phóng native memory.
 *
 * Thread safety:
 *  - Predictor của DJL KHÔNG thread-safe → dùng synchronized cho predict().
 *  - Acceptable vì pipeline embed batch tuần tự, runtime query nhanh (~50ms).
 *
 * Chỉ load khi `ai.rag.embedding.provider=local` (đây là default).
 */
@Service("localEmbeddingService")
@ConditionalOnProperty(name = "ai.rag.embedding.provider", havingValue = "local", matchIfMissing = true)
public class LocalEmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(LocalEmbeddingServiceImpl.class);

    private final RagProperties props;

    private volatile ZooModel<String, float[]> model;
    private volatile Predictor<String, float[]> predictor;
    private final Object initLock = new Object();
    private final Object predictLock = new Object();

    /**
     * Cache embedQuery — query của user thường lặp (vài câu hỏi phổ biến).
     * Bỏ qua DJL predict (~50-150ms) cho query đã cache.
     * Chỉ cache embedQuery, KHÔNG cache embedDocument (tốn RAM, mỗi xe khác nhau).
     */
    private final Cache<String, float[]> queryCache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(Duration.ofHours(1))
            .recordStats()
            .build();

    public LocalEmbeddingServiceImpl(RagProperties props) {
        this.props = props;
    }

    /**
     * Pre-warm model + tokenizer ngay khi BE ready, để query đầu tiên không bị
     * thêm 3-5s do lazy init. Chạy ASYNC để không chặn startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupAfterStartup() {
        Thread.startVirtualThread(() -> {
            try {
                long t0 = System.currentTimeMillis();
                log.info("[warmup] Initializing local embedding model in background...");
                ensureInitialized();
                // Chạy 1 predict thật để JIT warm code path
                synchronized (predictLock) {
                    predictor.predict("Khoi dong he thong tu van xe");
                }
                log.info("[warmup] Local embedding ready after {}ms",
                        System.currentTimeMillis() - t0);
            } catch (Exception ex) {
                // Non-fatal — sẽ thử lại khi có request thật
                log.warn("[warmup] Failed (non-fatal, sẽ retry on first call): {}", ex.getMessage());
            }
        });
    }

    @Override
    public float[] embedDocument(String text) {
        // KHÔNG cache document — tốn RAM, hash đã ở DB-level
        return embed(text, EmbeddingTaskType.RETRIEVAL_DOCUMENT);
    }

    @Override
    public float[] embedQuery(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Query text must not be null/blank");
        }
        // Cache theo trim() để query " hello " và "hello" share cache
        String key = text.trim();
        float[] cached = queryCache.getIfPresent(key);
        if (cached != null) {
            log.debug("Query embed cache HIT (key length={})", key.length());
            return cached.clone(); // clone để caller không vô tình modify cache
        }
        float[] vec = embed(key, EmbeddingTaskType.RETRIEVAL_QUERY);
        queryCache.put(key, vec.clone());
        return vec;
    }

    @Override
    public float[] embed(String text, EmbeddingTaskType taskType) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to embed must not be null/blank");
        }

        ensureInitialized();

        try {
            String input = truncateIfNeeded(text);
            float[] vector;
            synchronized (predictLock) {
                vector = predictor.predict(input);
            }
            if (vector == null || vector.length == 0) {
                throw new BusinessException("Local embedding returned empty vector");
            }
            if (props.getEmbedding().getLocal().isNormalize()) {
                normalizeInPlace(vector);
            }
            return vector;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Local embedding predict failed: {}", ex.getMessage(), ex);
            throw new BusinessException("Local embedding failed: " + ex.getMessage());
        }
    }

    // ==================== Internals ====================

    private void ensureInitialized() {
        if (predictor != null) return;
        synchronized (initLock) {
            if (predictor != null) return;
            try {
                long start = System.currentTimeMillis();
                RagProperties.Local cfg = props.getEmbedding().getLocal();
                log.info("Initializing local embedding model: url='{}', engine='{}'",
                        cfg.getModelUrl(), cfg.getEngine());

                Criteria<String, float[]> criteria = Criteria.builder()
                        .setTypes(String.class, float[].class)
                        .optModelUrls(cfg.getModelUrl())
                        .optEngine(cfg.getEngine())
                        .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                        .build();

                this.model = criteria.loadModel();
                this.predictor = model.newPredictor();

                long elapsed = System.currentTimeMillis() - start;
                log.info("Local embedding model loaded in {}ms (model={}, dim={})",
                        elapsed,
                        props.getEmbedding().getModel(),
                        props.getEmbedding().getDimensions());
            } catch (ModelNotFoundException | MalformedModelException | IOException ex) {
                log.error("Failed to load local embedding model: {}", ex.getMessage(), ex);
                throw new BusinessException("Cannot load local embedding model: " + ex.getMessage());
            }
        }
    }

    private String truncateIfNeeded(String text) {
        // Heuristic: 4 ký tự ≈ 1 token. maxLength * 4 = giới hạn ký tự gần đúng
        // để tránh tokenizer truncate ngầm và giữ phần đầu (thường giàu thông tin nhất).
        int charLimit = props.getEmbedding().getLocal().getMaxLength() * 4;
        if (text.length() <= charLimit) return text;
        log.debug("Truncating embed input from {} → {} chars", text.length(), charLimit);
        return text.substring(0, charLimit);
    }

    private static void normalizeInPlace(float[] v) {
        double sumSq = 0.0;
        for (float x : v) sumSq += x * x;
        if (sumSq <= 1e-12) return;
        float norm = (float) Math.sqrt(sumSq);
        for (int i = 0; i < v.length; i++) v[i] /= norm;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (predictor != null) {
                predictor.close();
                predictor = null;
            }
            if (model != null) {
                model.close();
                model = null;
            }
            log.info("Local embedding model resources released");
        } catch (Exception ex) {
            log.warn("Error during local embedding cleanup: {}", ex.getMessage());
        }
    }
}
