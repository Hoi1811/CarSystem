package web.car_system.Car_Service.domain.dto.audit;

import org.hibernate.envers.RevisionType;

import java.time.Instant;

/**
 * Một dòng trong timeline "thay đổi gần đây" — dùng cho /api/v1/audits/recent.
 */
public record RevisionSummaryDTO(
        int revisionId,
        Instant timestamp,
        String username,
        String entityType,
        String entityTypeDisplay,
        Object entityId,
        String entityLabel,
        RevisionType revisionType
) {}
