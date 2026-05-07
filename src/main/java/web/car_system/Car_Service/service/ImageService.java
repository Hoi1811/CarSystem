package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.image.BulkDeleteImagesResponseDTO;
import web.car_system.Car_Service.domain.dto.image.CarImageStatusDTO;
import web.car_system.Car_Service.domain.dto.image.CarImagesResponseDTO;

import java.io.IOException;
import java.util.List;

public interface ImageService {

    List<CarImagesResponseDTO> uploadImages(Integer carId, MultipartFile[] files) throws IOException;

    GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>> getImagesByCarId(Integer carId);

    String uploadManufacturerThumbnail(MultipartFile file) throws IOException;

    String uploadCarTypeThumbnail(MultipartFile file) throws IOException;

    String uploadCarThumbnail(Integer carId, MultipartFile file) throws IOException;

    /** Bulk image management endpoints. */
    Page<CarImageStatusDTO> findImageStatus(String filter, String q, Pageable pageable);

    String setThumbnailFromExistingImage(Integer carId, Integer imageId);

    void deleteImage(Integer carId, Integer imageId);

    BulkDeleteImagesResponseDTO deleteImagesBulk(List<Integer> imageIds);
}
