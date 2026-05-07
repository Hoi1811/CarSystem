package web.car_system.Car_Service.domain.dto.image;

import java.util.List;

public record BulkDeleteImagesResponseDTO(
        List<Integer> deletedImageIds,
        List<Integer> notFoundImageIds,
        List<Integer> clearedThumbnailCarIds
) {
}
