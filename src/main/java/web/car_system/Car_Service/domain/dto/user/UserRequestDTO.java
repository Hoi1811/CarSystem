package web.car_system.Car_Service.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dùng cho create/update user
public record UserRequestDTO(
        @NotBlank String email,
        @Size(max = 100) String fullName,
        @Pattern(regexp = "google|github|local") String provider
) {}

