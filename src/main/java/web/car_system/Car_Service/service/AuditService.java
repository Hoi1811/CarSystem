package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.audit.AuditDiffDTO;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.dto.audit.EntityHistoryEntryDTO;
import web.car_system.Car_Service.domain.dto.audit.EntitySnapshotDTO;
import web.car_system.Car_Service.domain.dto.audit.RevisionSummaryDTO;
import web.car_system.Car_Service.domain.entity.Car;

import java.time.Instant;
import java.util.List;

public interface AuditService {

    // ===== Legacy (giữ cho BC với FE cũ — nếu có) =====
    List<AuditLogDTO<Car>> getCarHistory(Integer carId);
    Car getCarAtRevision(Integer carId, Integer revisionNumber);

    // ===== Generic API (Phase A của AUDIT_LOG_UI_PLAN) =====

    /**
     * Lịch sử của 1 entity cụ thể, paginate theo revision desc.
     */
    Page<EntityHistoryEntryDTO> getEntityHistory(String entityType, Integer entityId, Pageable pageable);

    /**
     * Snapshot entity tại 1 revision cụ thể — fields được flatten thành Map.
     */
    EntitySnapshotDTO getEntitySnapshot(String entityType, Integer entityId, int revisionId);

    /**
     * Timeline tất cả thay đổi gần đây trên 9 entity audited.
     * @param entityTypeFilter null = tất cả; nếu khác = filter theo code (vd "car").
     * @param usernameFilter null = tất cả.
     * @param fromTs / toTs null = không filter.
     */
    Page<RevisionSummaryDTO> getRecentRevisions(
            String entityTypeFilter, String usernameFilter,
            Instant fromTs, Instant toTs, Pageable pageable);

    /**
     * So sánh 2 revision của cùng 1 entity, trả danh sách field đã đổi.
     */
    AuditDiffDTO diff(String entityType, Integer entityId, int fromRev, int toRev);
}
