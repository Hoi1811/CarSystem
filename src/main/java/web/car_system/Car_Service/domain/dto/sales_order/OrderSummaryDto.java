package web.car_system.Car_Service.domain.dto.sales_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simplified order info for list views
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    
    private Long id;
    private String orderNumber;
    
    private String customerName;
    private String customerPhone;
    
    private String carName;
    private String carModel;
    private String carColor;
    
    private String salesStaffName;
    
    private BigDecimal totalPrice;
    private BigDecimal remainingAmount;
    
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
}
