package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;

/**
 * DTO for order status distribution
 * Used for Pie/Doughnut charts showing order breakdown by status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusStatDto {
    
    private OrderStatus status;
    private Long count;
    
    // For display purposes
    private String statusLabel;
}
