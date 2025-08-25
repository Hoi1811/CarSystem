package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.service.AuditService;

import java.util.List;


@RestApiV1
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping(Endpoint.V1.AUDIT.CAR_AUDIT_HISTORY)
    public ResponseEntity<List<AuditLogDTO<Car>>> getCarAuditHistory(@PathVariable Integer id) {
        List<AuditLogDTO<Car>> history = auditService.getCarHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping(Endpoint.V1.AUDIT.CAR_AT_REVISION)
    public ResponseEntity<Car> getCarAtRevision(
            @PathVariable Integer id,
            @PathVariable Integer revisionNumber) {
        Car carState = auditService.getCarAtRevision(id, revisionNumber);
        return ResponseEntity.ok(carState);
    }
}
