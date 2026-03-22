package web.car_system.Car_Service.domain.dto.comparison;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ComparisonRule — avoids exposing the JPA entity directly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRuleDto {
    private Integer id;
    private String code;
    private String description;
}
