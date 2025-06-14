package web.car_system.Car_Service.domain.dto.car_segment;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record CarSegmentBatchCreateDTO(
        @NotNull(message = "Group ID không được để trống")
        Integer groupId,

        @NotNull(message = "Danh sách segment không được để trống")
        List<CarSegmentItemDTO> segments
) {
    public record CarSegmentItemDTO(
            @NotBlank(message = "Tên phân khúc không được để trống")
            @Size(max = 100, message = "Tên phân khúc không vượt quá 100 ký tự")
            String name,

            @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
            String description
    ) {}
}
