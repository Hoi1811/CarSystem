package web.car_system.Car_Service.domain.dto.manufacturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record ManufacturerCreateDTO(
        @NotBlank(message = "Tên hãng sản xuất không được để trống")
        @Size(max = 100, message = "Tên hãng sản xuất không vượt quá 100 ký tự")
        String name,

        MultipartFile thumbnailFile
) {
}
