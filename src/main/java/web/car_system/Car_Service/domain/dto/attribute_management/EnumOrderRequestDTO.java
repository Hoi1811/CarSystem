package web.car_system.Car_Service.domain.dto.attribute_management;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EnumOrderRequestDTO(
        @NotBlank(message = "Key của giá trị không được để trống (ví dụ: 'AT', 'MT')")
        String valueKey,

        @NotBlank(message = "Giá trị hiển thị không được để trống (ví dụ: 'Tự động (AT)')")
        String displayValue,

        @NotNull(message = "Thứ hạng (rank) không được để trống")
        Integer rank
) {}