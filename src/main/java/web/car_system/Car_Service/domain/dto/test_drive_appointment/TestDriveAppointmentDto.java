package web.car_system.Car_Service.domain.dto.test_drive_appointment;

import lombok.Builder;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.AppointmentStatus;
import web.car_system.Car_Service.domain.entity.Gender;

import java.time.LocalDateTime;

@Data
@Builder
public class TestDriveAppointmentDto {
    private Long id;

    // Thông tin khách hàng
    private String customerName;
    private String phoneNumber;
    private String email;
    private Integer age;
    private Gender gender;

    // Thông tin lịch hẹn
    private LocalDateTime preferredDateTime;
    private LocalDateTime confirmedDateTime;
    private AppointmentStatus appointmentStatus;
    private String customerNotes;
    private String adminNotes;

    // Thông tin tóm tắt về các đối tượng liên quan
    private CarSummaryDto carSummary;
    private UserSummaryDto assignee;

    // DTO lồng để chứa thông tin tóm tắt
    @Data
    @Builder
    public static class CarSummaryDto {
        private Long id;
        private String name; // Ví dụ: "Toyota Vios 1.5G"
        private String vin;
    }

    @Data
    @Builder
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
    }
}
