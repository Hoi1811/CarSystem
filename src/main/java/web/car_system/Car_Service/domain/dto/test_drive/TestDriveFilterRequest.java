package web.car_system.Car_Service.domain.dto.test_drive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.AppointmentStatus;

import java.time.LocalDate;

/**
 * DTO cho search/filter test drive appointments
 * Hỗ trợ tìm kiếm theo nhiều tiêu chí
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveFilterRequest {
    
    /**
     * Từ khóa tìm kiếm (search trong customerName, phone, email)
     */
    private String keyword;
    
    /**
     * Lọc theo trạng thái appointment (PENDING, CONFIRMED, COMPLETED, CANCELLED)
     */
    private AppointmentStatus status;
    
    /**
     * Lọc theo ngày hẹn lái thử từ (appointmentDate >= fromDate)
     */
    private LocalDate fromDate;
    
    /**
     * Lọc theo ngày hẹn lái thử đến (appointmentDate <= toDate)
     */
    private LocalDate toDate;
    
    /**
     * Lọc theo ID xe (inventoryCarId)
     */
    private Long inventoryCarId;
    
    /**
     * Lọc theo nhân viên phụ trách (assignedUserId)
     */
    private Long assignedUserId;
}
