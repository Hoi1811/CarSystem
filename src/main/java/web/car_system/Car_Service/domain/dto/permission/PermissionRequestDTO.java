package web.car_system.Car_Service.domain.dto.permission;

import jakarta.validation.constraints.NotBlank;

public record PermissionRequestDTO(
        @NotBlank String name,
        String description
) {}