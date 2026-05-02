package web.car_system.Car_Service.domain.dto.audit;

import org.hibernate.envers.RevisionType;

import java.time.Instant;
import java.util.Map;

/**
 * Snapshot của 1 entity tại 1 revision cụ thể — dùng cho
 * {@code GET /api/v1/audits/{entityType}/{id}/revisions/{revisionNumber}}.
 *
 * <p>Theo quyết định v1 trong {@code AUDIT_LOG_UI_PLAN.md}: dùng {@link Map} flatten
 * thay cho DTO chuyên biệt 9 entity — dễ maintain, FE phải biết tên field.</p>
 */
public record EntitySnapshotDTO(
        String entityType,
        Object entityId,
        int revisionId,
        Instant timestamp,
        String username,
        RevisionType revisionType,
        Map<String, Object> fields
) {}
