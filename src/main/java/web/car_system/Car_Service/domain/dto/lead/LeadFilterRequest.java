package web.car_system.Car_Service.domain.dto.lead;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.LeadRequestType;
import web.car_system.Car_Service.domain.entity.LeadStatus;

import java.time.LocalDate;

/**
 * DTO cho search/filter leads
 * Hỗ trợ tìm kiếm theo nhiều tiêu chí
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadFilterRequest {
    
    /**
     * Từ khóa tìm kiếm (search trong customerName, phone, email)
     */
    private String keyword;
    
    /**
     * Lọc theo trạng thái lead
     */
    private LeadStatus status;
    
    /**
     * Lọc theo nhân viên phụ trách (assigneeId)
     */
    private Long assigneeId;
    
    /**
     * Lọc theo ngày tạo từ (createdAt >= fromDate)
     */
    private LocalDate fromDate;
    
    /**
     * Lọc theo ngày tạo đến (createdAt <= toDate)
     */
    private LocalDate toDate;
    
    /**
     * Lọc theo loại yêu cầu (CONSULTATION, TEST_DRIVE, PURCHASE_INTENT)
     */
    private LeadRequestType requestType;
    
    /**
     * Lọc theo ID xe quan tâm (interestedCarId)
     */
    private Long inventoryCarId;
}
