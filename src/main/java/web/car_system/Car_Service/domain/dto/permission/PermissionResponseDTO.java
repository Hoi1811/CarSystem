package web.car_system.Car_Service.domain.dto.permission;

public record PermissionResponseDTO(
        Long permissionId,
        String name,
        String description
) {}