package web.car_system.Car_Service.domain.dto.car_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CarTypeCreateDTO(
        @NotBlank(message = "Tên loại xe không được để trống")
        @Size(max = 100, message = "Tên loại xe không vượt quá 100 ký tự")
        String name,

        @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
        String description,

        MultipartFile thumbnailFile
) {
}
