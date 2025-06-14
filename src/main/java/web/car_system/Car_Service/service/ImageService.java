package web.car_system.Car_Service.service;

import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.image.CarImagesResponseDTO;
import web.car_system.Car_Service.domain.entity.Image;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    List<Image> uploadImages(Integer carId, MultipartFile[] files) throws IOException, IllegalAccessError;
    GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>> getImagesByCarId(Integer carId);
    String uploadManufacturerThumbnail(MultipartFile file) throws IOException;
    String uploadCarTypeThumbnail(MultipartFile file) throws IOException;
    String uploadCarThumbnail(Integer carId, MultipartFile file) throws IOException;
}
