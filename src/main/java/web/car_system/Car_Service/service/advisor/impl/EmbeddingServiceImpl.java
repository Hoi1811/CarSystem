package web.car_system.Car_Service.service.advisor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import web.car_system.Car_Service.config.RagProperties;
import web.car_system.Car_Service.domain.dto.advisor.EmbeddingTaskType;
import web.car_system.Car_Service.domain.dto.advisor.GeminiEmbedRequest;
import web.car_system.Car_Service.domain.dto.advisor.GeminiEmbedResponse;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.service.advisor.EmbeddingService;

import java.util.List;

/**
 * Gọi Gemini embedContent qua RestClient (blocking) — tránh xung đột Netty/Tomcat
 * đã từng gây crash 0xC0000005 trong project.
 *
 * Có retry exponential backoff cho lỗi tạm thời (429, 5xx, network).
 *
 * Chỉ load khi `ai.rag.embedding.provider=gemini`. Mặc định project dùng "local".
 */
@Service("geminiEmbeddingService")
@ConditionalOnProperty(name = "ai.rag.embedding.provider", havingValue = "gemini")
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    private final RestClient restClient;
    private final RagProperties props;
    private final String apiKey;

    public EmbeddingServiceImpl(RestClient restClient,
                                RagProperties props,
                                @Value("${google.gemini.api.key}") String apiKey) {
        this.restClient = restClient;
        this.props = props;
        this.apiKey = apiKey;
    }

    @Override
    public float[] embedDocument(String text) {
        return embed(text, EmbeddingTaskType.RETRIEVAL_DOCUMENT);
    }

    @Override
    public float[] embedQuery(String text) {
        return embed(text, EmbeddingTaskType.RETRIEVAL_QUERY);
    }

    @Override
    public float[] embed(String text, EmbeddingTaskType taskType) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to embed must not be null/blank");
        }

        RagProperties.Embedding cfg = props.getEmbedding();
        String url = String.format("%s/models/%s:embedContent?key=%s",
                cfg.getBaseUrl(), cfg.getModel(), apiKey);

        GeminiEmbedRequest body = GeminiEmbedRequest.of(
                cfg.getModel(), text, taskType, cfg.getDimensions());

        int maxRetries = Math.max(1, cfg.getMaxRetries());
        Throwable lastError = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                GeminiEmbedResponse resp = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(GeminiEmbedResponse.class);

                List<Float> values = (resp != null && resp.embedding() != null)
                        ? resp.embedding().values() : null;
                if (values == null || values.isEmpty()) {
                    throw new BusinessException("Gemini embedding response empty (attempt " + attempt + ")");
                }
                if (values.size() != cfg.getDimensions()) {
                    log.warn("Gemini returned {} dims, expected {}. Using returned size.",
                            values.size(), cfg.getDimensions());
                }
                return toFloatArray(values);

            } catch (HttpClientErrorException.TooManyRequests | ResourceAccessException
                     | HttpServerErrorException ex) {
                lastError = ex;
                long backoffMs = 500L * (1L << (attempt - 1));
                log.warn("Embedding attempt {}/{} failed ({}). Retrying in {}ms",
                        attempt, maxRetries, ex.getClass().getSimpleName(), backoffMs);
                sleepQuiet(backoffMs);
            } catch (Exception ex) {
                log.error("Embedding non-retryable error: {}", ex.getMessage());
                throw new BusinessException("Gemini embedding failed: " + ex.getMessage());
            }
        }

        throw new BusinessException("Gemini embedding failed after " + maxRetries + " attempts: "
                + (lastError == null ? "unknown" : lastError.getMessage()));
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
