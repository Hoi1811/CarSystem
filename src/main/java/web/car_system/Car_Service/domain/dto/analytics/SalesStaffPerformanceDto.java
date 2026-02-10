package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesStaffPerformanceDto {
    
    private Long staffId;
    private String staffName;
    private Long totalOrders;
    private Long completedOrders;
    private BigDecimal totalRevenue;
    private Double conversionRate; // Completed / Total
}
