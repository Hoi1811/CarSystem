package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.permission.PermissionRequestDTO;
import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;
import web.car_system.Car_Service.service.PermissionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    // Tạo permission mới
    @PostMapping
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO>> createPermission(
            @RequestBody @Valid PermissionRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> response = permissionService.createPermission(request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.status(201).body(response); // 201 Created
        }
        return ResponseEntity.status(400).body(response);
    }

    // Lấy thông tin permission bằng ID
    @GetMapping("/{permissionId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO>> getPermissionById(
            @PathVariable Long permissionId) {
        GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> response = permissionService.getPermissionById(permissionId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    // Lấy danh sách tất cả permissions (phân trang)
    @GetMapping
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<PermissionResponseDTO>>> getAllPermissions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") short pageSize) {
        GlobalResponseDTO<PaginatedMeta, List<PermissionResponseDTO>> response = permissionService.getAllPermissions(keyword, pageIndex, pageSize);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    // Cập nhật permission
    @PutMapping("/{permissionId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO>> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody @Valid PermissionRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> response = permissionService.updatePermission(permissionId, request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    // Xóa permission
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deletePermission(@PathVariable Long permissionId) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = permissionService.deletePermission(permissionId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.status(404).body(response);
    }
}