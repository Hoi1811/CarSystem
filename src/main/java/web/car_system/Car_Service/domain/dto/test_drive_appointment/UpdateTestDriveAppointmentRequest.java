package web.car_system.Car_Service.domain.dto.test_drive_appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.AppointmentStatus;
import web.car_system.Car_Service.domain.entity.Gender;

import java.time.LocalDateTime;

@Data
public class UpdateTestDriveAppointmentRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private AppointmentStatus appointmentStatus; // Admin có thể thay đổi trạng thái

    private LocalDateTime confirmedDateTime; // Admin có thể set/thay đổi giờ đã chốt

    private Long assigneeId; // ID của nhân viên được gán

    private String adminNotes; // Ghi chú của Admin

    private String customerName;

    private String phoneNumber;

    private String email;

    private Integer age;

    private Gender gender;

    private LocalDateTime preferredDateTime; // Thời gian mong muốn của khách (Admin có thể điều chỉnh)

    private String customerNotes; // Ghi chú của khách (Admin có thể sửa)

    private Long inventoryCarId; // Xe đăng ký lái thử (Admin có thể đổi xe khác cho khách)
}
