package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.entity.CarStatus;
import web.car_system.Car_Service.service.CarService;

@RestApiV1
@RequiredArgsConstructor
public class CarApprovalController {

    private final CarService carService;

    @GetMapping("/cars/approval-status")
    public ResponseEntity<?> getCarsByApprovalStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam String status) {
        
        CarStatus carStatus = CarStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(carService.getCarsByApprovalStatus(carStatus, page));
    }

    @PatchMapping("/cars/{id}/approval-status")
    public ResponseEntity<?> updateApprovalStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        
        CarStatus newStatus = CarStatus.valueOf(status.toUpperCase());
        carService.updateApprovalStatus(id, newStatus);
        return ResponseEntity.ok("Cập nhật trạng thái phê duyệt xe thành công.");
    }
}
