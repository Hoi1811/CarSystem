package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.domain.dto.staging.BulkStagingActionRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingImportRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingResponseDto;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;
import web.car_system.Car_Service.service.StagingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/crawler/staging")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
public class StagingController {

    private final StagingService stagingService;

    // API 1: Import Raw Data
    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestBody List<StagingImportRequest> requests) {
        int count = stagingService.importCrawledData(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(count + " records imported to staging successfully.");
    }

    // API 2: Get Staging Data with Validation Flags
    @GetMapping
    public ResponseEntity<Page<StagingResponseDto>> getStagingData(
            @RequestParam(defaultValue = "PENDING_REVIEW") StagingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(stagingService.getStagingDataWithValidation(status, pageable));
    }

    // API 3: Manual Update Normalized Data (Fixes by human)
    @PutMapping("/{id}")
    public ResponseEntity<StagingResponseDto> manualUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updatedNormalizedSpecs) {
        return ResponseEntity.ok(stagingService.manualUpdateNormalizedData(id, updatedNormalizedSpecs));
    }

    // API 4: Bulk Approve Staging Data to Main DB
    @PostMapping("/approve")
    public ResponseEntity<String> approveData(@Valid @RequestBody BulkStagingActionRequest request) {
        int approvedCount = stagingService.approveStagingCars(request);
        return ResponseEntity.ok(approvedCount + " cars approved and moved to main inventory.");
    }

    // API 5: Bulk Delete/Reject Staging Data
    @DeleteMapping
    public ResponseEntity<Void> deleteData(@Valid @RequestBody BulkStagingActionRequest request) {
        stagingService.deleteStagingCars(request);
        return ResponseEntity.noContent().build();
    }
}
