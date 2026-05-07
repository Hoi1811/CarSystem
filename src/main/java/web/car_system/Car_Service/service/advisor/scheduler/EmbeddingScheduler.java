package web.car_system.Car_Service.service.advisor.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import web.car_system.Car_Service.service.advisor.EmbeddingPipelineService;

/**
 * Cron wrapper. Bean chỉ tồn tại khi `ai.rag.scheduler.enabled=true` —
 * dễ tắt khi demo offline / staging mà không sửa code.
 */
@Component
@ConditionalOnProperty(name = "ai.rag.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddingScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingScheduler.class);

    private final EmbeddingPipelineService pipeline;

    public EmbeddingScheduler(EmbeddingPipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @Scheduled(cron = "${ai.rag.scheduler.cron:0 0 2 * * *}")
    public void runNightly() {
        log.info("====== Nightly embedding job triggered ======");
        try {
            EmbeddingPipelineService.EmbedStats stats = pipeline.regenerateAll();
            log.info("Nightly embedding finished: {}", stats);
        } catch (Exception ex) {
            log.error("Nightly embedding job FAILED", ex);
        }
    }
}
