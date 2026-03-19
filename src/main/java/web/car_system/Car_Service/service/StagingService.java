package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.staging.BulkStagingActionRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingImportRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingResponseDto;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;

import java.util.List;
import java.util.Map;

public interface StagingService {
    
    /**
     * Nhận dữ liệu thô từ crawler và đưa vào staging area.
     */
    int importCrawledData(List<StagingImportRequest> requests);

    /**
     * Lấy danh sách xe trong staging, tự động thực hiện map (nếu chưa map) và 
     * gán thêm các cờ validate (Xanh/Đỏ/Vàng) trước khi trả về.
     */
    Page<StagingResponseDto> getStagingDataWithValidation(StagingStatus status, Pageable pageable);

    /**
     * Admin fix lỗi bằng cách ghi đè thông số chuẩn hóa.
     */
    StagingResponseDto manualUpdateNormalizedData(Long stagingId, Map<String, Object> updatedNormalizedSpecs);

    /**
     * Chấp nhận bản nháp và tự động tạo xe trong hệ thống chính.
     */
    int approveStagingCars(BulkStagingActionRequest request);

    /**
     * Xóa bỏ các bản nháp rác.
     */
    void deleteStagingCars(BulkStagingActionRequest request);
}
