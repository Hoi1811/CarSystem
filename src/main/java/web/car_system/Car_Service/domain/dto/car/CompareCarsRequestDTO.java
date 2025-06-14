package web.car_system.Car_Service.domain.dto.car;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompareCarsRequestDTO(
        @NotEmpty(message = "Không được trống, cần có xe để so sánh")
        @Size(max = 4, min = 2)
        List<Integer> ids
) {

}
