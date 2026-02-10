package web.car_system.Car_Service.domain.dto.sales_order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng tối đa 100 ký tự")
    private String customerName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ (phải 10 số, bắt đầu bằng 0)")
    private String customerPhone;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String customerEmail;
    
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String customerAddress;
    
    @Size(max = 20, message = "Số CCCD/CMND tối đa 20 ký tự")
    private String customerIdNumber;
    
    @NotNull(message = "Xe cần đặt hàng không được để trống")
    private Long inventoryCarId;
    
    private Long salesStaffId; // Optional, có thể assign sau
    
    private Long leadId; // Optional, nếu convert từ lead
    
    @NotNull(message = "Giá xe không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá xe phải lớn hơn 0")
    private BigDecimal basePrice;
    
    @DecimalMin(value = "0.0", message = "Phí bổ sung không được âm")
    private BigDecimal additionalFees;
    
    @DecimalMin(value = "0.0", message = "Giảm giá không được âm")
    private BigDecimal discountAmount;
    
    @DecimalMin(value = "0.0", message = "Tiền cọc không được âm")
    private BigDecimal depositAmount;
    
    @Future(message = "Ngày giao xe dự kiến phải trong tương lai")
    private LocalDate expectedDeliveryDate;
    
    @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
    private String notes;
    
    @Size(max = 1000, message = "Ghi chú nội bộ tối đa 1000 ký tự")
    private String internalNotes;
}
