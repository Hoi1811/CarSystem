package web.car_system.Car_Service.domain.dto.role;

import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;

import java.util.Set;

public record RoleResponseDTO(
        Long roleId,
        String name,
        String description,
        Set<PermissionResponseDTO> permissions
) {}