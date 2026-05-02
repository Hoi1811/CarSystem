package web.car_system.Car_Service.domain.dto.audit;

import org.hibernate.envers.RevisionType;

import java.time.Instant;

/**
 * Một entry trong lịch sử của 1 entity — dùng cho
 * {@code GET /api/v1/audits/{entityType}/{id}/history}.
 */
public record EntityHistoryEntryDTO(
        int revisionId,
        Instant timestamp,
        String username,
        RevisionType revisionType
) {}
