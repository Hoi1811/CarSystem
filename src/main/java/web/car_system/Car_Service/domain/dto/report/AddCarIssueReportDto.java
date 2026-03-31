package web.car_system.Car_Service.domain.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCarIssueReportDto(
        @NotNull(message = "ID của xe không được để trống")
        Integer carId,
        
        @NotBlank(message = "Nội dung báo cáo không được để trống")
        String description
) {
}
