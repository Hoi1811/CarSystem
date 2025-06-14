package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.role.RoleRequestDTO;
import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;

import java.util.List;

public interface RoleService {
    GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> createRole(RoleRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> getRoleById(Long roleId);
    GlobalResponseDTO<PaginatedMeta, List<RoleResponseDTO>> getAllRoles(String keyword, int pageIndex, short pageSize);
    GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> updateRole(Long roleId, RoleRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, Void> deleteRole(Long roleId);
    GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> addPermissionToRole(Long roleId, String permissionName);
    GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> removePermissionFromRole(Long roleId, String permissionName);
    GlobalResponseDTO<NoPaginatedMeta, List<String>> getRolesByUserId(Long userId);
    GlobalResponseDTO<NoPaginatedMeta, List<String>> getPermissionsByUserId(Long userId);
}