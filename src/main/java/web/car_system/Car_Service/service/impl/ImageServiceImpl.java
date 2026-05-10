package web.car_system.Car_Service.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.image.BulkDeleteImagesResponseDTO;
import web.car_system.Car_Service.domain.dto.image.CarImageStatusDTO;
import web.car_system.Car_Service.domain.dto.image.CarImagesResponseDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.Image;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.ImageRepository;
import web.car_system.Car_Service.service.ImageService;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final CarRepository carRepository;
    private final ImageRepository imageRepository;
    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB

    private enum ImageFormat { JPEG, PNG, GIF, WEBP, AVIF, UNKNOWN }
    private static final Set<ImageFormat> ALLOWED_FORMATS = EnumSet.of(
            ImageFormat.JPEG, ImageFormat.PNG, ImageFormat.GIF,
            ImageFormat.WEBP, ImageFormat.AVIF
    );
    private static final int TARGET_WIDTH = 800;
    private static final int TARGET_HEIGHT = 600;

    private static final String FOLDER_CAR_IMAGES = "car_images";
    private static final String FOLDER_CAR_THUMBNAILS = "car_thumbnails";
    private static final String FOLDER_MANUFACTURER_THUMBNAILS = "manufacturer_thumbnails";
    private static final String FOLDER_CAR_TYPE_THUMBNAILS = "car_type_thumbnails";

    // ============================================================
    // UPLOAD: gallery images cho 1 xe (có dedupe theo MD5)
    // ============================================================
    @Override
    @Transactional
    public List<CarImagesResponseDTO> uploadImages(Integer carId, MultipartFile[] files) throws IOException {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + carId));

        List<CarImagesResponseDTO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            validateImageFile(file);
            byte[] bytes = file.getBytes();
            String fileHash = computeMd5(bytes);

            Optional<Image> existing = imageRepository.findFirstByCarCarIdAndFileHash(carId, fileHash);
            if (existing.isPresent()) {
                log.info("Skip duplicate upload car={} hash={} → reuse imageId={}", carId, fileHash, existing.get().getImageId());
                result.add(toDto(existing.get()));
                continue;
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "public_id", "car_" + carId + "_" + System.currentTimeMillis(),
                    "folder", FOLDER_CAR_IMAGES,
                    "transformation", new Transformation()
                            .width(TARGET_WIDTH).height(TARGET_HEIGHT)
                            .crop("pad").background("black")
            ));
            String publicId = (String) uploadResult.get("public_id");

            Image image = new Image();
            image.setCar(car);
            image.setUrl(publicId);
            image.setFileHash(fileHash);
            result.add(toDto(imageRepository.save(image)));
        }
        return result;
    }

    // ============================================================
    // GET ảnh theo carId (giữ nguyên signature cũ)
    // ============================================================
    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>> getImagesByCarId(Integer carId) {
        try {
            List<CarImagesResponseDTO> dtos = imageRepository.findByCarCarId(carId).stream()
                    .map(this::toDto)
                    .toList();
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarImagesResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy ảnh thành công").build())
                    .data(dtos)
                    .build();
        } catch (Exception e) {
            log.error("getImagesByCarId failed for carId={}", carId, e);
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarImagesResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Lỗi khi lấy ảnh: " + e.getMessage()).build())
                    .data(new ArrayList<>())
                    .build();
        }
    }

    // ============================================================
    // UPLOAD thumbnail Manufacturer / CarType / Car
    // ============================================================
    @Override
    @Transactional
    public String uploadManufacturerThumbnail(MultipartFile file) throws IOException {
        validateImageFile(file);
        Map<?, ?> r = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", FOLDER_MANUFACTURER_THUMBNAILS,
                "public_id", "manufacturer_" + System.currentTimeMillis(),
                "resource_type", "auto"
        ));
        return (String) r.get("public_id");
    }

    @Override
    @Transactional
    public String uploadCarTypeThumbnail(MultipartFile file) throws IOException {
        validateImageFile(file);
        Map<?, ?> r = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", FOLDER_CAR_TYPE_THUMBNAILS,
                "public_id", "car_type_" + System.currentTimeMillis(),
                "resource_type", "auto"
        ));
        return (String) r.get("public_id");
    }

    @Override
    @Transactional
    public String uploadCarThumbnail(Integer carId, MultipartFile file) throws IOException {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + carId));
        validateImageFile(file);

        Map<?, ?> r = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", FOLDER_CAR_THUMBNAILS,
                "public_id", "car_thumbnail_" + carId + "_" + System.currentTimeMillis(),
                "resource_type", "auto",
                "transformation", new Transformation()
                        .width(TARGET_WIDTH).height(TARGET_HEIGHT)
                        .crop("pad").background("black")
        ));
        String publicId = (String) r.get("public_id");

        car.setThumbnail(publicId);
        carRepository.save(car);
        return publicId;
    }

    // ============================================================
    // BULK image management
    // ============================================================
    @Override
    public Page<CarImageStatusDTO> findImageStatus(String filter, String q, Pageable pageable) {
        String normalizedFilter = (filter == null || filter.isBlank()) ? "ALL" : filter.toUpperCase();
        if (!normalizedFilter.equals("ALL") && !normalizedFilter.equals("MISSING") && !normalizedFilter.equals("COMPLETE")) {
            throw new IllegalArgumentException("filter phải là một trong: ALL | MISSING | COMPLETE");
        }
        return carRepository.findImageStatus(normalizedFilter, q, pageable);
    }

    @Override
    @Transactional
    public String setThumbnailFromExistingImage(Integer carId, Integer imageId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + carId));
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + imageId));
        if (image.getCar() == null || !image.getCar().getCarId().equals(carId)) {
            throw new IllegalArgumentException("Ảnh " + imageId + " không thuộc xe " + carId);
        }
        car.setThumbnail(image.getUrl());
        carRepository.save(car);
        log.info("Set thumbnail for car={} from imageId={} (publicId={})", carId, imageId, image.getUrl());
        return image.getUrl();
    }

    @Override
    @Transactional
    public void deleteImage(Integer carId, Integer imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + imageId));
        if (image.getCar() == null || !image.getCar().getCarId().equals(carId)) {
            throw new IllegalArgumentException("Ảnh " + imageId + " không thuộc xe " + carId);
        }
        Car car = image.getCar();
        String publicId = image.getUrl();

        destroyOnCloudinary(publicId);
        imageRepository.delete(image);

        if (publicId != null && publicId.equals(car.getThumbnail())) {
            car.setThumbnail(null);
            carRepository.save(car);
            log.info("Cleared thumbnail of car={} because deleted image was the active thumbnail", carId);
        }
    }

    @Override
    @Transactional
    public BulkDeleteImagesResponseDTO deleteImagesBulk(List<Integer> imageIds) {
        List<Image> found = imageRepository.findAllById(imageIds);
        Map<Integer, Image> foundById = new HashMap<>();
        for (Image img : found) {
            foundById.put(img.getImageId(), img);
        }

        List<Integer> notFound = new ArrayList<>();
        List<Integer> deleted = new ArrayList<>();
        List<Integer> clearedThumbnailCarIds = new ArrayList<>();

        for (Integer id : imageIds) {
            Image img = foundById.get(id);
            if (img == null) {
                notFound.add(id);
                continue;
            }
            String publicId = img.getUrl();
            destroyOnCloudinary(publicId);

            Car car = img.getCar();
            imageRepository.delete(img);
            deleted.add(id);

            if (car != null && publicId != null && publicId.equals(car.getThumbnail())) {
                car.setThumbnail(null);
                carRepository.save(car);
                clearedThumbnailCarIds.add(car.getCarId());
            }
        }
        log.info("Bulk delete images: requested={} deleted={} notFound={} thumbnailsCleared={}",
                imageIds.size(), deleted.size(), notFound.size(), clearedThumbnailCarIds.size());
        return new BulkDeleteImagesResponseDTO(deleted, notFound, clearedThumbnailCarIds);
    }

    // ============================================================
    // Helpers
    // ============================================================
    private CarImagesResponseDTO toDto(Image image) {
        CarImagesResponseDTO dto = new CarImagesResponseDTO();
        dto.setImageId(image.getImageId());
        dto.setUrl(image.getUrl());
        dto.setFileHash(image.getFileHash());
        dto.setCarId(image.getCar() != null ? image.getCar().getCarId() : null);
        return dto;
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file vượt quá 20MB: " + file.getOriginalFilename());
        }
        ImageFormat format = detectImageFormat(file.getBytes());
        if (!ALLOWED_FORMATS.contains(format)) {
            throw new IllegalArgumentException(
                    "File không phải ảnh hợp lệ (chỉ chấp nhận JPEG/PNG/GIF/WebP/AVIF): "
                            + file.getOriginalFilename()
            );
        }
    }

    /**
     * Nhận diện định dạng ảnh dựa trên magic bytes (file signature).
     * Đáng tin cậy hơn ImageIO vì không phụ thuộc decoder, và bắt được trường hợp
     * file bị đặt sai đuôi (ví dụ WebP đổi tên thành .jpeg).
     */
    private ImageFormat detectImageFormat(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return ImageFormat.UNKNOWN;

        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return ImageFormat.JPEG;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A
                && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A) {
            return ImageFormat.PNG;
        }
        // GIF: "GIF8"
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
            return ImageFormat.GIF;
        }
        // WebP: "RIFF" ???? "WEBP"
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ImageFormat.WEBP;
        }
        // AVIF: ???? "ftyp" "avif" hoặc "avis"
        if (bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p'
                && bytes[8] == 'a' && bytes[9] == 'v' && bytes[10] == 'i'
                && (bytes[11] == 'f' || bytes[11] == 's')) {
            return ImageFormat.AVIF;
        }
        return ImageFormat.UNKNOWN;
    }

    private String computeMd5(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available on this JVM", e);
        }
    }

    private void destroyOnCloudinary(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (Exception e) {
            // Don't fail the DB delete if Cloudinary cleanup fails — log and move on.
            log.warn("Cloudinary destroy failed for publicId={}: {}", publicId, e.getMessage());
        }
    }
}
