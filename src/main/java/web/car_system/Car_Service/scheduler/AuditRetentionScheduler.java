package web.car_system.Car_Service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import web.car_system.Car_Service.service.AuditRetentionService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "audit.retention.enabled", havingValue = "true")
public class AuditRetentionScheduler {

    private final AuditRetentionService auditRetentionService;

    @Scheduled(cron = "${audit.retention.cron:0 0 3 1 * *}")
    public void purgeOldAuditRevisions() {
        log.info("========== Audit Retention Job Started ==========");
        try {
            long deleted = auditRetentionService.purgeUsingConfiguredRetention();
            log.info("Audit retention completed — total rows deleted: {}", deleted);
        } catch (Exception e) {
            log.error("Audit retention job failed", e);
        }
        log.info("========== Audit Retention Job Completed ==========");
    }
}
