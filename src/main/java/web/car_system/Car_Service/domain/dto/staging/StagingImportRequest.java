package web.car_system.Car_Service.domain.dto.staging;

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
    private String name;
    private String model;
    private Integer year;
    private BigDecimal price;
    private Map<String, Object> rawSpecifications;
}
