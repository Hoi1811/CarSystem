package web.car_system.Car_Service.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
        @Pattern(regexp = "^$|^[0-9+\\-\\s]+$", message = "Số điện thoại không hợp lệ")
        String phone
) {}
