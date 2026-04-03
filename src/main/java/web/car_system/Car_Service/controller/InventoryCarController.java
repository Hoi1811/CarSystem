package web.car_system.Car_Service.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.dto.inventory_car.UpdateSaleStatusRequest;
import web.car_system.Car_Service.service.InventoryCarService;
import web.car_system.Car_Service.utility.ResponseFactory;

import java.util.List;
import java.util.UUID;

import static web.car_system.Car_Service.constant.Endpoint.V1.INVENTORY_CAR.*;

@RestApiV1
@RequiredArgsConstructor
public class InventoryCarController {
    private final InventoryCarService inventoryCarService;
    private final web.car_system.Car_Service.service.UserActivityLogService activityLogService;  // ✅ Track user behavior

    // === PUBLIC ENDPOINTS ===

    @GetMapping(GET_ALL_AVAILABLE)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<InventoryCarDto>>> getAllAvailableCars(
            @RequestParam(required = false) Integer carId,
            @RequestParam(required = false) Long showroomId,
            @PageableDefault(size = 12) Pageable pageable) {

        Page<InventoryCarDto> carPage = inventoryCarService.getAllAvailableCars(carId, showroomId, pageable);
        return ResponseFactory.successPageable(carPage, "Lấy danh sách xe thành công.");
    }

    @GetMapping(GET_DETAILS_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> getInventoryCarDetails(
            @PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest request) {

        // Fetch car details once — used for both response and activity tracking
        InventoryCarDto carDetails = inventoryCarService.getInventoryCarDetails(id);

        // Track user activity (failure must never affect the main response)
        try {
            HttpSession session = request.getSession(false);
            String sessionId = session != null ? session.getId() : UUID.randomUUID().toString();
            String ipAddress = request.getRemoteAddr();
            Long userId = getCurrentUserId();

            if (carDetails != null && carDetails.getCarId() != null) {
                activityLogService.logViewInventoryCar(
                    userId,
                    sessionId,
                    id,
                    carDetails.getCarId(),
                    ipAddress
                );
            }
        } catch (Exception e) {
            // Tracking failed — ignore, still return result
        }

        return ResponseFactory.success(carDetails, "Lấy chi tiết xe thành công");
    }
    
    /**
     * Helper: Get current user ID if authenticated
     */
    private Long getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                String username = auth.getName();
                try {
                    return Long.parseLong(username);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    // === ADMIN ENDPOINTS ===

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'SYSTEM_ADMIN')")
    @GetMapping(GET_ALL_FOR_ADMIN)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<InventoryCarDto>>> getAllInventoryCarsForAdmin(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryCarDto> cars = inventoryCarService.getAllInventoryCarsForAdmin(pageable);
        return ResponseFactory.successPageable(cars, "Lấy danh sách xe trong kho thành công");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PostMapping(ADD_TO_INVENTORY)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> addCarToInventory(
            @Valid @RequestBody CreateInventoryCarRequest request) {
        InventoryCarDto newCar = inventoryCarService.addCarToInventory(request);
        return ResponseFactory.success(newCar, "Thêm xe vào kho thành công", HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PutMapping(UPDATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> updateInventoryCar(
            @PathVariable Long id, @Valid @RequestBody CreateInventoryCarRequest request) {
        InventoryCarDto updatedCar = inventoryCarService.updateInventoryCar(id, request);
        return ResponseFactory.success(updatedCar, "Cập nhật thông tin xe thành công", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'SYSTEM_ADMIN')")
    @PatchMapping(UPDATE_STATUS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, InventoryCarDto>> updateSaleStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateSaleStatusRequest request) {
        InventoryCarDto updatedCar = inventoryCarService.updateSaleStatus(id, request.getSaleStatus());
        return ResponseFactory.success(updatedCar, "Cập nhật trạng thái bán hàng thành công", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @DeleteMapping(DELETE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteInventoryCar(@PathVariable Long id) {
        inventoryCarService.deleteInventoryCar(id);
        return ResponseFactory.success(null, "Xóa xe khỏi kho thành công", HttpStatus.NO_CONTENT);
    }
}
