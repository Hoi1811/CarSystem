package web.car_system.Car_Service.domain.dto.payment;

import jakarta.validation.constraints.*;
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
public class AddPaymentRequest {
    
    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
    
    @NotNull(message = "Loại thanh toán không được để trống")
    private PaymentType paymentType;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
    
    private LocalDateTime transactionDate; // Optional, defaults to now
    
    @Size(max = 100, message = "Mã giao dịch tối đa 100 ký tự")
    private String transactionReference;
    
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;
}
