package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.staging.BulkStagingActionRequest;
import web.car_system.Car_Service.domain.dto.staging.ManualUpdateStagingRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingImportRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingResponseDto;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;
import web.car_system.Car_Service.service.StagingService;

import java.util.List;

import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequestMapping("/api/v1/admin/crawler/staging")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
public class StagingController {

    private final StagingService stagingService;

    // API 1: Import Raw Data
    @PostMapping("/import")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Integer>> importData(
            @Valid @RequestBody @Size(max = 500, message = "Không thể import quá 500 bản ghi trong một lần") List<@Valid StagingImportRequest> requests) {
        int count = stagingService.importCrawledData(requests);
        return success(count, count + " bản ghi đã được import vào staging.", HttpStatus.CREATED);
    }

    // API 2: Get Staging Data with Validation Flags
    @GetMapping
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<StagingResponseDto>>> getStagingData(
            @RequestParam(defaultValue = "PENDING_REVIEW") StagingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StagingResponseDto> result = stagingService.getStagingDataWithValidation(status, pageable);
        return successPageable(result, "Lấy dữ liệu staging thành công.");
    }

    // API 3: Manual Update Normalized Data (Fixes by human)
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, StagingResponseDto>> manualUpdate(
            @PathVariable Long id,
            @Valid @RequestBody ManualUpdateStagingRequest request) {
        StagingResponseDto result = stagingService.manualUpdateNormalizedData(id, request.getNormalizedSpecifications());
        return success(result, "Cập nhật dữ liệu chuẩn hóa thành công.");
    }

    // API 4: Bulk Approve Staging Data to Main DB
    @PostMapping("/approve")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Integer>> approveData(@Valid @RequestBody BulkStagingActionRequest request) {
        int approvedCount = stagingService.approveStagingCars(request);
        return success(approvedCount, approvedCount + " bản ghi đã được duyệt và chuyển vào hệ thống chính.");
    }

    // API 5: Bulk Delete/Reject Staging Data
    @DeleteMapping
    public ResponseEntity<Void> deleteData(@Valid @RequestBody BulkStagingActionRequest request) {
        stagingService.deleteStagingCars(request);
        return ResponseEntity.noContent().build();
    }
}
