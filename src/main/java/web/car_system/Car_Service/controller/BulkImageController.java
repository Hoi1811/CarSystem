package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.image.BulkDeleteImagesRequestDTO;
import web.car_system.Car_Service.domain.dto.image.BulkDeleteImagesResponseDTO;
import web.car_system.Car_Service.domain.dto.image.CarImageStatusDTO;
import web.car_system.Car_Service.service.ImageService;

import java.util.List;
import java.util.Map;

import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

/**
 * Bulk image management cho admin: liệt kê xe + trạng thái ảnh, set thumbnail
 * từ ảnh existing, xóa lẻ / xóa nhiều ảnh.
 *
 * Tách khỏi {@link CarController} để giữ controller chính gọn, vì module này
 * có thể phình thêm trong tương lai (filter nâng cao, batch import ảnh, v.v.).
 */
@RestApiV1
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
public class BulkImageController {

    private final ImageService imageService;

    @GetMapping(Endpoint.V1.CAR.ADMIN_CAR_IMAGE_STATUS)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<CarImageStatusDTO>>> getImageStatus(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("carId").descending());
        Page<CarImageStatusDTO> result = imageService.findImageStatus(filter, q, pageable);
        return successPageable(result, "Lấy danh sách trạng thái ảnh thành công");
    }

    @PutMapping(Endpoint.V1.CAR.ADMIN_CAR_THUMBNAIL_FROM_IMAGE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Map<String, String>>> setThumbnailFromImage(
            @PathVariable Integer carId,
            @PathVariable Integer imageId) {
        String publicId = imageService.setThumbnailFromExistingImage(carId, imageId);
        return success(Map.of("thumbnail", publicId), "Cập nhật ảnh đại diện thành công");
    }

    @DeleteMapping(Endpoint.V1.CAR.ADMIN_CAR_DELETE_IMAGE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteImage(
            @PathVariable Integer carId,
            @PathVariable Integer imageId) {
        imageService.deleteImage(carId, imageId);
        return success(null, "Xóa ảnh thành công", HttpStatus.OK);
    }

    @PostMapping(Endpoint.V1.CAR.ADMIN_CAR_BULK_DELETE_IMAGES)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, BulkDeleteImagesResponseDTO>> bulkDeleteImages(
            @Valid @RequestBody BulkDeleteImagesRequestDTO request) {
        BulkDeleteImagesResponseDTO body = imageService.deleteImagesBulk(request.imageIds());
        return success(body, "Đã xóa " + body.deletedImageIds().size() + " ảnh");
    }
}
