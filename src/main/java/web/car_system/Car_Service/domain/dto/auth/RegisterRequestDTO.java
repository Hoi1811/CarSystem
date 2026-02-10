package web.car_system.Car_Service.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3-50 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ chứa chữ, số và dấu gạch dưới")
        String username,

        @NotBlank(message = "Password không được để trống")
        @Size(min = 8, max = 100, message = "Password tối thiểu 8 ký tự")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
                message = "Password phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
        )
        String password,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        boolean rememberMe
) {
}
