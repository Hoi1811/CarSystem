package web.car_system.Car_Service.domain.dto.staging;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingImportRequest {
    @NotBlank(message = "Tên xe không được để trống")
    private String name;
    private String model;
    private Integer year;
    private BigDecimal price;
    private Map<String, Object> rawSpecifications;
}
