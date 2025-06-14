package web.car_system.Car_Service.domain.dto.car_segment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CarSegmentUpdateDTO(
        @NotBlank(message = "Tên phân khúc không được để trống")
        @Size(max = 100, message = "Tên phân khúc không vượt quá 100 ký tự")
        String name,

        @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
        String description,

        @NotNull(message = "Nhóm phân khúc không được để trống")
        Integer groupId
) {}
