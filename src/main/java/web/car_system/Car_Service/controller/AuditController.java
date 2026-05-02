package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.audit.AuditDiffDTO;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.dto.audit.EntityHistoryEntryDTO;
import web.car_system.Car_Service.domain.dto.audit.EntitySnapshotDTO;
import web.car_system.Car_Service.domain.dto.audit.RevisionSummaryDTO;
import web.car_system.Car_Service.domain.entity.AuditedEntityType;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.service.AuditService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestApiV1
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
public class AuditController {

    private final AuditService auditService;

    // ===== Legacy endpoints (giữ cho BC) =====

    @GetMapping(Endpoint.V1.AUDIT.CAR_AUDIT_HISTORY)
    public ResponseEntity<List<AuditLogDTO<Car>>> getCarAuditHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(auditService.getCarHistory(id));
    }

    @GetMapping(Endpoint.V1.AUDIT.CAR_AT_REVISION)
    public ResponseEntity<Car> getCarAtRevision(
            @PathVariable Integer id,
            @PathVariable Integer revisionNumber) {
        return ResponseEntity.ok(auditService.getCarAtRevision(id, revisionNumber));
    }

    // ===== Generic endpoints (Phase A) =====

    @GetMapping(Endpoint.V1.AUDIT.TYPES)
    public ResponseEntity<List<Map<String, String>>> listSupportedEntityTypes() {
        return ResponseEntity.ok(
                Arrays.stream(AuditedEntityType.values())
                        .map(t -> Map.of(
                                "code", t.getCode(),
                                "displayName", t.getDisplayName()))
                        .toList());
    }

    @GetMapping(Endpoint.V1.AUDIT.ENTITY_HISTORY)
    public ResponseEntity<Page<EntityHistoryEntryDTO>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Integer id,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getEntityHistory(entityType, id, pageable));
    }

    @GetMapping(Endpoint.V1.AUDIT.ENTITY_AT_REVISION)
    public ResponseEntity<EntitySnapshotDTO> getEntityAtRevision(
            @PathVariable String entityType,
            @PathVariable Integer id,
            @PathVariable Integer revisionNumber) {
        return ResponseEntity.ok(auditService.getEntitySnapshot(entityType, id, revisionNumber));
    }

    @GetMapping(Endpoint.V1.AUDIT.ENTITY_DIFF)
    public ResponseEntity<AuditDiffDTO> getEntityDiff(
            @PathVariable String entityType,
            @PathVariable Integer id,
            @RequestParam("from") Integer fromRev,
            @RequestParam("to") Integer toRev) {
        return ResponseEntity.ok(auditService.diff(entityType, id, fromRev, toRev));
    }

    @GetMapping(Endpoint.V1.AUDIT.RECENT)
    public ResponseEntity<Page<RevisionSummaryDTO>> getRecentRevisions(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(
                auditService.getRecentRevisions(entityType, username, from, to, pageable));
    }
}
