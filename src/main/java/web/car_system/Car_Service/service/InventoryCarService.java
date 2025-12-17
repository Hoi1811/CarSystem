package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.SaleStatus;

public interface InventoryCarService {
    /**
     * Thêm một chiếc xe cụ thể vào kho, dựa trên một mẫu xe đã có.
     * @param request DTO chứa thông tin xe cần thêm và carId của mẫu xe.
     * @return DTO của chiếc xe vừa được tạo.
     */
    InventoryCarDto addCarToInventory(CreateInventoryCarRequest request);

    /**
     * Lấy danh sách tất cả các xe đang bán (AVAILABLE) cho khách hàng xem, có phân trang.
     * @param pageable Thông tin phân trang.
     * @return Một trang (Page) chứa các InventoryCarDto.
     */
    Page<InventoryCarDto> getAllAvailableCars(Integer carId, Pageable pageable);

    /**
     * Lấy danh sách TẤT CẢ xe trong kho cho Admin quản lý, có phân trang.
     * @param pageable Thông tin phân trang.
     * @return Một trang (Page) chứa các InventoryCarDto.
     */
    Page<InventoryCarDto> getAllInventoryCarsForAdmin(Pageable pageable);

    /**
     * Lấy thông tin chi tiết của một chiếc xe trong kho.
     * @param id ID của xe trong kho.
     * @return DTO chi tiết của xe.
     */
    InventoryCarDto getInventoryCarDetails(Long id);

    /**
     * Cập nhật thông tin của một chiếc xe trong kho.
     * @param id ID của xe cần cập nhật.
     * @param request DTO chứa thông tin mới.
     * @return DTO của chiếc xe sau khi đã cập nhật.
     */
    InventoryCarDto updateInventoryCar(Long id, CreateInventoryCarRequest request);

    /**
     * Cập nhật nhanh trạng thái bán hàng của một xe.
     * @param id ID của xe cần cập nhật.
     * @param newStatus Trạng thái mới (AVAILABLE, RESERVED, SOLD).
     * @return DTO của chiếc xe sau khi đã cập nhật.
     */
    InventoryCarDto updateSaleStatus(Long id, SaleStatus newStatus);

    /**
     * Xóa (soft delete) một chiếc xe khỏi kho.
     * @param id ID của xe cần xóa.
     */
    void deleteInventoryCar(Long id);
}
