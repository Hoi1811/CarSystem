package web.car_system.Car_Service.domain.dto.audit;

import java.time.Instant;
import java.util.List;

/**
 * Diff giữa 2 revision của cùng 1 entity — dùng cho
 * {@code GET /api/v1/audits/{entityType}/{id}/diff?from=&to=}.
 */
public record AuditDiffDTO(
        String entityType,
        Object entityId,
        int fromRevision,
        Instant fromTimestamp,
        int toRevision,
        Instant toTimestamp,
        List<FieldChange> changes
) {
    public record FieldChange(String field, Object before, Object after) {}
}
