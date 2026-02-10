package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly/yearly revenue data points
 * Used for Line/Bar charts showing revenue trends over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendDto {
    
    private Integer month;  // 1-12
    private Integer year;   // e.g., 2024
    private BigDecimal amount;
    
    // Constructor for JPQL projection
    public RevenueTrendDto(Integer month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }
}
