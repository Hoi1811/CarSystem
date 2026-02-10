package web.car_system.Car_Service.domain.dto.sales_order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating existing orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {
    
    @Size(max = 100, message = "Tên khách hàng tối đa 100 ký tự")
    private String customerName;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String customerEmail;
    
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String customerAddress;
    
    @Size(max = 20, message = "Số CCCD/CMND tối đa 20 ký tự")
    private String customerIdNumber;
    
    private Long salesStaffId;
    
    @DecimalMin(value = "0.0", message = "Phí bổ sung không được âm")
    private BigDecimal additionalFees;
    
    @DecimalMin(value = "0.0", message = "Giảm giá không được âm")
    private BigDecimal discountAmount;
    
    private OrderStatus orderStatus;
    
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    
    @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
    private String notes;
    
    @Size(max = 1000, message = "Ghi chú nội bộ tối đa 1000 ký tự")
    private String internalNotes;
    
    @Size(max = 500, message = "Lý do hủy tối đa 500 ký tự")
    private String cancellationReason;
}
