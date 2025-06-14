package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.permission.PermissionRequestDTO;
import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;

import java.util.List;

public interface PermissionService {
    GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> createPermission(PermissionRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> getPermissionById(Long permissionId);
    GlobalResponseDTO<PaginatedMeta, List<PermissionResponseDTO>> getAllPermissions(String keyword, int pageIndex, short pageSize);
    GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> updatePermission(Long permissionId, PermissionRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, Void> deletePermission(Long permissionId);
}