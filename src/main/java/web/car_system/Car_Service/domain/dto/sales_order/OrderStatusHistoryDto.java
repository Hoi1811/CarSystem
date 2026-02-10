package web.car_system.Car_Service.domain.dto.sales_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDto {
    
    private Long id;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String changedByName;
    private String changeReason;
    private LocalDateTime changedAt;
}
