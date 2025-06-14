package web.car_system.Car_Service.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.car.CarDetailsResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.image.CarImagesResponseDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.Image;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.ImageRepository;
import web.car_system.Car_Service.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final CarRepository carRepository;

    private final ImageRepository imageRepository;

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final int TARGET_WIDTH = 800; // Kích thước mục tiêu
    private static final int TARGET_HEIGHT = 600;


    @Override
    @Transactional
    public List<Image> uploadImages(Integer carId, MultipartFile[] files) throws IOException, IllegalAccessError {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + carId));

        List<Image> uploadedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            // Kiểm tra file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("One or more files are empty");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File size exceeds 20MB limit: " + file.getOriginalFilename());
            }
            String contentType = file.getContentType();
            if (!ALLOWED_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Only JPEG, PNG, GIF files are allowed: " + file.getOriginalFilename());
            }
            BufferedImage imageCheck = ImageIO.read(file.getInputStream());
            if (imageCheck == null) {
                throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
            }

            // Upload lên Cloudinary với resize và padding
            // Upload lên Cloudinary với resize và padding
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", "car_" + carId + "_" + System.currentTimeMillis(),
                    "folder", "car_images",
                    "transformation", Arrays.asList(
                            // Chuyển đổi kích thước và thêm padding
                            Map.of("width", TARGET_WIDTH),
                            Map.of("height", TARGET_HEIGHT),
                            Map.of("crop", "pad"),
                            Map.of("background", "black")
                    )
            ));
            String imageUrl = (String) uploadResult.get("public_id");

            // Lưu thông tin vào database
            Image image = new Image();
            image.setCar(car);
            image.setUrl(imageUrl);
            uploadedImages.add(imageRepository.save(image));
        }

        return uploadedImages;
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>> getImagesByCarId(Integer carId) {
        try{
            List<Image> images = imageRepository.findByCarCarId(carId);
            List<CarImagesResponseDTO> carImagesResponseDTO = images.stream()
                    .map(this::convertToDTO)
                    .toList();
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarImagesResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Lấy thông tin xe thành công")
                            .build())
                    .data(carImagesResponseDTO)
                    .build();
        }catch (Exception e){
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarImagesResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi lấy thông tin ảnh: " + e.getMessage())
                            .build())
                    .data(new ArrayList<>())
                    .build();
        }

    }
    private CarImagesResponseDTO convertToDTO(Image image) {
        CarImagesResponseDTO dto = new CarImagesResponseDTO();
        dto.setImageId(image.getImageId());
        dto.setUrl(image.getUrl());
        dto.setFileHash(image.getFileHash());
        dto.setCarId(image.getCar().getCarId());
        return dto;
    }

    @Override
    @Transactional
    public String uploadManufacturerThumbnail(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file vượt quá 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh JPEG hoặc PNG");
        }

        // Check if valid image
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("File không phải là ảnh hợp lệ");
        }

        // Upload original image (NO TRANSFORMATION)
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "manufacturer_thumbnails",
                "public_id", "manufacturer_" + System.currentTimeMillis(),
                "resource_type", "auto" // Tự động nhận định dạng
        ));

        return (String) uploadResult.get("public_id");
    }
    @Override
    @Transactional
    public String uploadCarTypeThumbnail(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file vượt quá 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh JPEG hoặc PNG");
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("File không phải là ảnh hợp lệ");
        }

        // Upload to Cloudinary với thư mục riêng cho CarType
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "car_type_thumbnails",
                "public_id", "car_type_" + System.currentTimeMillis(),
                "resource_type", "auto" // Tự động nhận định dạng
        ));

        return (String) uploadResult.get("public_id");
    }

    @Override
    @Transactional
    public String uploadCarThumbnail(Integer carId, MultipartFile file) throws IOException {
        // Validate car existence
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + carId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 20MB limit: " + file.getOriginalFilename());
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF files are allowed: " + file.getOriginalFilename());
        }

        // Read file into bytes to avoid stream consumption
        byte[] fileBytes = file.getBytes();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
        if (image == null) {
            // Save file to disk for inspection
            File tempFile = new File("temp_" + file.getOriginalFilename());
            file.transferTo(tempFile);
            System.out.println("Saved temp file to: " + tempFile.getAbsolutePath());
            throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
        }

        // Upload to Cloudinary with specific folder and transformations
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "car_thumbnails",
                "public_id", "car_thumbnail_" + carId + "_" + System.currentTimeMillis(),
                "resource_type", "auto",
                "transformation", new Transformation()
                        .width(TARGET_WIDTH)
                        .height(TARGET_HEIGHT)
                        .crop("pad")
                        .background("black")
        ));

        String publicId = (String) uploadResult.get("public_id");

        // Update car's thumbnail field
        car.setThumbnail(publicId);
        carRepository.save(car);

        return publicId;
    }
}
