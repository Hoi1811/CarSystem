package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.service.AuditRetentionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "audit.retention.enabled", havingValue = "true")
public class AuditRetentionServiceImpl implements AuditRetentionService {

    private static final List<String> AUD_TABLES = List.of(
            "cars_aud",
            "attributes_aud",
            "specifications_aud",
            "comparison_rules_aud",
            "car_types_aud",
            "manufacturers_aud",
            "car_segments_aud",
            "car_segment_groups_aud",
            "car_issue_reports_aud",
            "car_car_types_aud"
    );

    private static final String REVISION_TABLE = "custom_revision_entity";

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${audit.retention.months:12}")
    private int retentionMonths;

    @Override
    @Transactional
    public long purgeUsingConfiguredRetention() {
        long cutoff = Instant.now().minus(retentionMonths * 30L, ChronoUnit.DAYS).toEpochMilli();
        log.info("[AuditRetention] cutoff = {} ({} months ago)", cutoff, retentionMonths);
        return purgeOlderThan(cutoff);
    }

    @Override
    @Transactional
    public long purgeOlderThan(long cutoffEpochMillis) {
        long total = 0;
        for (String table : AUD_TABLES) {
            String sql = "DELETE a FROM " + table + " a"
                    + " JOIN " + REVISION_TABLE + " r ON a.rev = r.revision_id"
                    + " WHERE r.revision_timestamp < :cutoff";
            try {
                Query q = entityManager.createNativeQuery(sql).setParameter("cutoff", cutoffEpochMillis);
                int deleted = q.executeUpdate();
                if (deleted > 0) {
                    log.info("[AuditRetention] {} → deleted {} rows", table, deleted);
                }
                total += deleted;
            } catch (Exception ex) {
                log.warn("[AuditRetention] skip {} — {}", table, ex.getMessage());
            }
        }

        String orphanSql = "DELETE FROM " + REVISION_TABLE
                + " WHERE revision_timestamp < :cutoff"
                + " AND revision_id NOT IN ("
                + buildUnionExistsSubquery()
                + ")";
        try {
            Query q = entityManager.createNativeQuery(orphanSql).setParameter("cutoff", cutoffEpochMillis);
            int deleted = q.executeUpdate();
            log.info("[AuditRetention] {} (orphans) → deleted {} rows", REVISION_TABLE, deleted);
            total += deleted;
        } catch (Exception ex) {
            log.warn("[AuditRetention] orphan revision cleanup failed — {}", ex.getMessage());
        }

        log.info("[AuditRetention] total deleted = {}", total);
        return total;
    }

    private String buildUnionExistsSubquery() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < AUD_TABLES.size(); i++) {
            if (i > 0) sb.append(" UNION ");
            sb.append("SELECT rev FROM ").append(AUD_TABLES.get(i));
        }
        return sb.toString();
    }
}
