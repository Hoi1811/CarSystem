package web.car_system.Car_Service.domain.dto.analytics;

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
public class OrderStatisticsDto {
    
    // Order counts by status
    private Map<String, Long> ordersByStatus;
    
    // Revenue metrics
    private BigDecimal totalRevenue;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    
    // Order counts
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long activeOrders;
    
    // Average metrics
    private BigDecimal averageOrderValue;
}
