package web.car_system.Car_Service.domain.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserRoleAssignDTO(
        @NotBlank String roleName
) {}