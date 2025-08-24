package web.car_system.Car_Service.domain.dto.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record AttributeRequestDTO(
        @NotBlank(message = "Tên thuộc tính không được để trống")
        String name,

        String description,

        @NotBlank(message = "Loại control không được để trống")
        String controlType, // Ví dụ: NUMBER_INPUT, SINGLE_SELECT

        String unit, // Đơn vị: kg, hp, mm

        @NotNull(message = "Trọng số không được để trống")
        @Positive(message = "Trọng số phải là số dương")
        Float weight,

        @NotNull(message = "Nhóm thông số không được để trống")
        Integer specificationId,

        @NotNull(message = "Luật so sánh không được để trống")
        Integer comparisonRuleId
) {}