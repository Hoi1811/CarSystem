package web.car_system.Car_Service.domain.dto.test_drive_appointment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.Gender;

import java.time.LocalDateTime;

@Data
public class CreateTestDriveAppointmentRequest {

    @NotEmpty(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotEmpty(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không hợp lệ (định dạng Việt Nam)")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    // Các trường mới bổ sung
    private Integer age;
    private Gender gender;

    @NotNull(message = "Vui lòng chọn thời gian mong muốn")
    @Future(message = "Thời gian lái thử phải là một thời điểm trong tương lai")
    private LocalDateTime preferredDateTime;

    private String customerNotes;

    @NotNull(message = "Cần có ID của xe để đăng ký lái thử")
    private Long inventoryCarId;
}
