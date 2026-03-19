package web.car_system.Car_Service.domain.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindRelatedCarsRequestDTO(
        @NotBlank(message = "Tên xe không được để trống")
        @Size(max = 200, message = "Tên xe không được vượt quá 200 ký tự")
        String name
) {
}
