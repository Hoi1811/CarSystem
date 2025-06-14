package web.car_system.Car_Service.domain.dto.car_segment_group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarSegmentGroupCreateDTO(
        @NotBlank(message = "Tên nhóm phân khúc không được để trống")
        @Size(max = 100, message = "Tên nhóm phân khúc không vượt quá 100 ký tự")
        String name,

        @Size(max = 1000, message = "Mô tả không vượt quá 1000 ký tự")
        String description
) {
}

