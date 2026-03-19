package web.car_system.Car_Service.domain.dto.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarSuggestionDto {
    private Integer    carId;
    private String     name;
    private String     model;
    private String     thumbnail;
    private BigDecimal price;
}