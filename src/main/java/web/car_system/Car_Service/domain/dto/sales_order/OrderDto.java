package web.car_system.Car_Service.domain.dto.sales_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Complete order details including car info, customer, and pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    
    private Long id;
    private String orderNumber;
    
    // Customer Info
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String customerIdNumber;
    
    // Car Info (embedded)
    private Long inventoryCarId;
    private String carName;        // From InventoryCar -> Car
    private String carModel;
    private String carColor;
    private String carVin;
    
    // Sales Staff Info
    private Long salesStaffId;
    private String salesStaffName;
    
    // Lead Info
    private Long leadId;
    
    // Pricing
    private BigDecimal basePrice;
    private BigDecimal additionalFees;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private BigDecimal depositAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    
    // Status
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    
    // Dates
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private LocalDate completedDate;
    private LocalDate cancelledDate;
    
    // Notes
    private String notes;
    private String cancellationReason;
    private String internalNotes;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
