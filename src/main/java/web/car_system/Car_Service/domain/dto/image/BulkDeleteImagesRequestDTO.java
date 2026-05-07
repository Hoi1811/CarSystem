package web.car_system.Car_Service.domain.dto.image;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkDeleteImagesRequestDTO(
        @NotEmpty(message = "Danh sách ảnh cần xóa không được rỗng")
        @Size(max = 200, message = "Không thể xóa quá 200 ảnh trong một lần")
        List<Integer> imageIds
) {
}
