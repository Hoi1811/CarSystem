package web.car_system.Car_Service.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.PaymentMethod;
import web.car_system.Car_Service.domain.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDto {
    
    private Long id;
    
    private Long salesOrderId;
    private String orderNumber;
    
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    
    private LocalDateTime transactionDate;
    private String transactionReference;
    
    private Long receivedById;
    private String receivedByName;
    
    private String notes;
    private LocalDateTime createdAt;
}
