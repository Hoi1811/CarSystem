package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.role.RoleRequestDTO;
import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;
import web.car_system.Car_Service.service.RoleService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    // Tạo role mới
    @PostMapping(Endpoint.V1.ROLE.ROLE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO>> createRole(
            @RequestBody @Valid RoleRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> response = roleService.createRole(request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.status(201).body(response); // 201 Created
        }
        return ResponseEntity.status(400).body(response);
    }

    // Lấy thông tin role bằng ID
    @GetMapping(Endpoint.V1.ROLE.ROLE_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO>> getRoleById(
            @PathVariable Long roleId) {
        GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> response = roleService.getRoleById(roleId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    // Lấy danh sách tất cả roles (phân trang)
    @GetMapping(Endpoint.V1.ROLE.ROLE)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<RoleResponseDTO>>> getAllRoles(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") short pageSize) {
        GlobalResponseDTO<PaginatedMeta, List<RoleResponseDTO>> response = roleService.getAllRoles(keyword, pageIndex, pageSize);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    // Cập nhật role
    @PutMapping(Endpoint.V1.ROLE.ROLE_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO>> updateRole(
            @PathVariable Long roleId,
            @RequestBody @Valid RoleRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> response = roleService.updateRole(roleId, request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    // Xóa role
    @DeleteMapping(Endpoint.V1.ROLE.ROLE_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteRole(@PathVariable Long roleId) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = roleService.deleteRole(roleId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.status(404).body(response);
    }

    // Thêm permission vào role
    @PostMapping(Endpoint.V1.ROLE.ROLE_PERMISSIONS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO>> addPermissionToRole(
            @PathVariable Long roleId,
            @RequestParam String permissionName) {
        GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> response = roleService.addPermissionToRole(roleId, permissionName);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    // Xóa permission khỏi role
    @DeleteMapping(Endpoint.V1.ROLE.ROLE_PERMISSIONS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO>> removePermissionFromRole(
            @PathVariable Long roleId,
            @RequestParam String permissionName) {
        GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> response = roleService.removePermissionFromRole(roleId, permissionName);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }
}