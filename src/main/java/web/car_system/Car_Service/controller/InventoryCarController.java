package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.SaleStatus;
import web.car_system.Car_Service.service.InventoryCarService;
import web.car_system.Car_Service.utility.ResponseFactory;

import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.INVENTORY_CAR.*;

@RestApiV1
@RequiredArgsConstructor
public class InventoryCarController {
    private final InventoryCarService inventoryCarService;

    // === PUBLIC ENDPOINTS ===

    @GetMapping(GET_ALL_AVAILABLE)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<InventoryCarDto>>> getAllAvailableCars(Pageable pageable) {
        Page<InventoryCarDto> cars = inventoryCarService.getAllAvailableCars(pageable);
        return ResponseFactory.success(cars, "Lấy danh sách xe đang bán thành công");
    }

    @GetMapping(GET_DETAILS_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> getInventoryCarDetails(@PathVariable Long id) {
        InventoryCarDto carDetails = inventoryCarService.getInventoryCarDetails(id);
        return ResponseFactory.success(carDetails, "Lấy chi tiết xe thành công");
    }


    // === ADMIN ENDPOINTS ===
    // Quan trọng: Bạn cần cấu hình Spring Security để bảo vệ các endpoint này

    @PostMapping(ADD_TO_INVENTORY)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> addCarToInventory(@Valid @RequestBody CreateInventoryCarRequest request) {
        InventoryCarDto newCar = inventoryCarService.addCarToInventory(request);
        return ResponseFactory.success(newCar, "Thêm xe vào kho thành công",HttpStatus.CREATED);
    }

    @GetMapping(GET_ALL_FOR_ADMIN)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<InventoryCarDto>> > getAllInventoryCarsForAdmin(Pageable pageable) {
        Page<InventoryCarDto> cars = inventoryCarService.getAllInventoryCarsForAdmin(pageable);
        return ResponseFactory.success(cars, "Lấy danh sách xe trong kho thành công");
    }

    @PutMapping(UPDATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> updateInventoryCar(@PathVariable Long id, @Valid @RequestBody CreateInventoryCarRequest request) {
        InventoryCarDto updatedCar = inventoryCarService.updateInventoryCar(id, request);
        return ResponseFactory.success(updatedCar, "Cập nhật thông tin xe thành công", HttpStatus.OK);
    }

    @PatchMapping(UPDATE_STATUS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> updateSaleStatus(@PathVariable Long id, @RequestParam SaleStatus newStatus) {
        InventoryCarDto updatedCar = inventoryCarService.updateSaleStatus(id, newStatus);
        return ResponseFactory.success(updatedCar, "Cập nhật trạng thái bán hàng thành công", HttpStatus.OK);
    }

    @DeleteMapping(DELETE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteInventoryCar(@PathVariable Long id) {
        inventoryCarService.deleteInventoryCar(id);
        return ResponseFactory.success(null, "Xóa xe khỏi kho thành công", HttpStatus.NO_CONTENT);
    }
}
