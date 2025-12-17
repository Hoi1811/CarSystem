package web.car_system.Car_Service.domain.dto.test_drive_appointment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.Gender;

import java.time.LocalDateTime;

@Data
public class CreateTestDriveAppointmentRequest {

    @NotEmpty(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String email;

    // Các trường mới bổ sung
    private Integer age;
    private Gender gender;

    @NotNull(message = "Vui lòng chọn thời gian mong muốn")
    private LocalDateTime preferredDateTime;

    private String customerNotes;

    @NotNull(message = "Cần có ID của xe để đăng ký lái thử")
    private Long inventoryCarId;
}
