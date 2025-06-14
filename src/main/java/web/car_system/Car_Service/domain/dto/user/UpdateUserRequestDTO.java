package web.car_system.Car_Service.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @NotBlank @Size(max = 100) String name,
        @Email String email
) {

}
