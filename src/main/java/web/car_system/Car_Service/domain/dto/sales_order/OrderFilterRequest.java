package web.car_system.Car_Service.domain.dto.sales_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.PaymentStatus;

import java.time.LocalDate;

/**
 * Filter DTO for searching/filtering sales orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    
    // Search by keyword (customer name, phone, order number)
    private String keyword;
    
    // Filter by status
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    
    // Filter by sales staff
    private Long salesStaffId;
    
    // Filter by date range
    private LocalDate fromDate; // Order date >= fromDate
    private LocalDate toDate;   // Order date <= toDate
    
    // Filter by inventory car
    private Long inventoryCarId;
    
    // Filter by lead (converted orders)
    private Long leadId;
    
    // Filter by price range
    private Double minPrice;
    private Double maxPrice;
}
