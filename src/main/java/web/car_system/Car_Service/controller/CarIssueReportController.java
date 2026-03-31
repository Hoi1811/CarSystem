package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.report.AddCarIssueReportDto;
import web.car_system.Car_Service.domain.entity.CarIssueReport;
import web.car_system.Car_Service.service.CarIssueReportService;

@RestApiV1
@RequiredArgsConstructor
public class CarIssueReportController {

    private final CarIssueReportService reportService;

    @PostMapping("/car-reports")
    public ResponseEntity<?> createReport(@Valid @RequestBody AddCarIssueReportDto request, Authentication authentication) {
        // Lấy ID của user đang login từ Security Context (Giả sử authorities hoặc principal cấp)
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        Long userId = Long.parseLong(authentication.getName()); // Tùy thuộc vào config Token trong app của bạn.
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(request, userId));
    }

    @GetMapping("/car-reports")
    public ResponseEntity<?> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(reportService.getReportsByReportStatus(CarIssueReport.ReportStatus.valueOf(status.toUpperCase()), pageable));
        }
        return ResponseEntity.ok(reportService.getAllReports(pageable));
    }

    @PatchMapping("/car-reports/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, CarIssueReport.ReportStatus.valueOf(status.toUpperCase())));
    }
}
