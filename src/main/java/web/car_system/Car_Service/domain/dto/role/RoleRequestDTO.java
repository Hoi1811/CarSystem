package web.car_system.Car_Service.domain.dto.role;


import jakarta.validation.constraints.NotBlank;

public record RoleRequestDTO(
        @NotBlank String name,
        String description
) {}