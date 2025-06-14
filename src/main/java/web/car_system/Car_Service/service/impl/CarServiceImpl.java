package web.car_system.Car_Service.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.car.*;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.projection.OptionProjection;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.CarMapper;
import web.car_system.Car_Service.exception.NotFoundException;
import web.car_system.Car_Service.repository.*;
import web.car_system.Car_Service.service.CarService;
import web.car_system.Car_Service.utility.CarDataCache;
import web.car_system.Car_Service.utility.CarSpecification;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    private final ImageRepository imageRepository;

    private final AttributeRepository attributeRepository;

    private final SpecificationRepository specificationRepository;

    private final CarAttributeRepository carAttributeRepository;

    private final CarTypeRepository carTypeRepository;

    private final CarDataCache carDataCache;

    private final Cloudinary cloudinary;

    @Value("${cloudinary.base-url}")
    private String baseUrl;

    private static final int TARGET_WIDTH = 800;
    private static final int TARGET_HEIGHT = 600;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_IMPORT_CAR_TYPES = Arrays.asList("json");


    // Chuyển giá dạng "299 triệu" thành BigDecimal
    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null) return null;
        String cleanedPrice = priceStr.replaceAll("[^0-9]", ""); // Lấy số từ "299 triệu"
        return new BigDecimal(cleanedPrice + "000000"); // Chuyển thành 299000000
    }

    @Override
    public GlobalResponseDTO<?, CarDetailsResponseDTO> createCar(AddCarRequestDTO carRequest) {
        try {
            CarDetailsResponseDTO response = CarMapper.INSTANCE.toCarDetailsResponse(carRepository.save(CarMapper.INSTANCE.toCar(carRequest)));
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Tạo xe thành công")
                            .build())
                    .data(response)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi tạo xe: " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"specifications", "attributes"}, allEntries = true, condition = "#result != null")
    public GlobalResponseDTO<?, CarDetailsResponseDTO> createCarV3(AddCarRequestDTO carRequest) {
        try {
            CarDetailsResponseDTO response = createCarV3Internal(carRequest);
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Tạo xe thành công (v3)")
                            .build())
                    .data(response)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi tạo xe (v3): " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }
    private CarDetailsResponseDTO createCarV3Internal(AddCarRequestDTO carRequest) throws Exception {
        Car car = CarMapper.INSTANCE.toCar(carRequest);

        // Xử lý carTypes nếu có
        if (carRequest.carTypeIds() != null && !carRequest.carTypeIds().isEmpty()) {
            List<CarType> carTypes = carTypeRepository.findAllById(carRequest.carTypeIds());
            car.setCarTypes(carTypes);
        }
        if(carRequest.origin() != null && !carRequest.origin().equals("")) {
            car.setOrigin(carRequest.origin());
        }
        car.setImages(new ArrayList<>()); // Khởi tạo danh sách images
        car = carRepository.save(car);
        // Base URL từ Cloudinary (có thể đưa vào application.properties)
        System.out.println("Catch bitch 1");

        if (carRequest.images() != null && !carRequest.images().isEmpty()) {
            List<Image> uploadedImages = new ArrayList<>();
            int thumbnailIndex = carRequest.thumbnailIndex() != null ? carRequest.thumbnailIndex() : 0;

            for (int i = 0; i < carRequest.images().size(); i++) {
                MultipartFile file = carRequest.images().get(i);

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

                String fileHash = calculateFileHash(file);
                String publicId = "car_" + car.getCarId() + "_" + System.currentTimeMillis(); // Tạo public_id một lần
                String fileName = publicId; // Đồng bộ fileName với public_id

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "public_id", publicId, // Dùng cùng public_id
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
                System.out.println("Catch bitch 2");
                Image image = new Image();
                image.setCar(car);
                image.setUrl(imageUrl); // Lưu tên file đồng bộ
                image.setFileHash(fileHash);
                if (i == thumbnailIndex) {
                    car.setThumbnail(imageUrl); // Lưu tên file đồng bộ
                }
                uploadedImages.add(image);
            }
            System.out.println("Catch bitch 3");
            imageRepository.saveAll(uploadedImages);
            car.setImages(uploadedImages);
            carRepository.save(car);
        }

        // Xử lý CarAttribute (nếu có gửi specifications)
        if (carRequest.specifications() != null && !carRequest.specifications().isEmpty()) {
            List<CarAttribute> carAttributes = new ArrayList<>();
            for (var specRequest : carRequest.specifications()) {
                Specification specification = (Specification) specificationRepository.findByName(specRequest.name())
                        .orElseThrow(() -> new IllegalArgumentException("Specification not found: " + specRequest.name()));

                for (var attrRequest : specRequest.attributes()) {
                    Attribute attribute = (Attribute) attributeRepository.findByNameAndSpecificationSpecificationId(attrRequest.name(), specification.getSpecificationId())
                            .orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + attrRequest.name()));

                    CarAttribute carAttribute = new CarAttribute();
                    carAttribute.setCar(car);
                    carAttribute.setAttribute(attribute);
                    carAttribute.setValue(attrRequest.value());
                    carAttributes.add(carAttribute);
                }
            }
            if (!carAttributes.isEmpty()) {
                carAttributeRepository.saveAll(carAttributes);
            }
        }
        return CarMapper.INSTANCE.toCarDetailsResponse(car);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"specifications", "attributes"}, allEntries = true, condition = "#result != null")
    public GlobalResponseDTO<?, CarDetailsResponseDTO> createCarV2(AddCarRequestDTO carRequest) {
        try {
            CarDetailsResponseDTO response = createCarV2Internal(carRequest);
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Tạo xe thành công (v2)")
                            .build())
                    .data(response)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi tạo xe (v2): " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }
    private CarDetailsResponseDTO createCarV2Internal(AddCarRequestDTO carRequest) throws Exception {
        Car car = CarMapper.INSTANCE.toCar(carRequest);

        // Xử lý carTypes nếu có
        if (carRequest.carTypeIds() != null && !carRequest.carTypeIds().isEmpty()) {
            List<CarType> carTypes = carTypeRepository.findAllById(carRequest.carTypeIds());
            car.setCarTypes(carTypes);
        }
        if(carRequest.origin() != null && !carRequest.origin().equals("")) {
            car.setOrigin(carRequest.origin());
        }


        car.setImages(new ArrayList<>()); // Khởi tạo danh sách images
        car = carRepository.save(car);
        // Base URL từ Cloudinary (có thể đưa vào application.properties)

        if (carRequest.images() != null && !carRequest.images().isEmpty()) {
            List<Image> uploadedImages = new ArrayList<>();
            int thumbnailIndex = carRequest.thumbnailIndex() != null ? carRequest.thumbnailIndex() : 0;

            for (int i = 0; i < carRequest.images().size(); i++) {
                MultipartFile file = carRequest.images().get(i);

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

                String fileHash = calculateFileHash(file);
                String publicId = "car_" + car.getCarId() + "_" + System.currentTimeMillis(); // Tạo public_id một lần
                String fileName = publicId; // Đồng bộ fileName với public_id
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "public_id", publicId, // Dùng cùng public_id
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
                Image image = new Image();
                image.setCar(car);
                image.setUrl(imageUrl); // Lưu tên file đồng bộ
                image.setFileHash(fileHash);
                if (i == thumbnailIndex) {
                    car.setThumbnail(imageUrl); // Lưu tên file đồng bộ
                }
                uploadedImages.add(image);
            }

            imageRepository.saveAll(uploadedImages);
            car.setImages(uploadedImages);
            carRepository.save(car);
        }
        Map<String, Specification> specMap = carDataCache.getSpecificationMap();
        Map<String, Attribute> attrMap = carDataCache.getAttributeMap();

        List<CarAttribute> carAttributes = new ArrayList<>();
        for (var specRequest : carRequest.specifications()) {
            Specification specification = specMap.get(specRequest.name());
            if (specification == null) {
                specification = carDataCache.addSpecification(specRequest.name());
                specMap.put(specRequest.name(), specification); // Cập nhật cache thủ công
            }

            for (var attrRequest : specRequest.attributes()) {
                String attrKey = attrRequest.name() + "_" + specification.getSpecificationId();
                Attribute attribute = attrMap.get(attrKey);
                if (attribute == null) {
                    attribute = carDataCache.addAttribute(attrRequest.name(), specification);
                    attrMap.put(attrKey, attribute); // Cập nhật cache thủ công
                }

                CarAttribute carAttribute = new CarAttribute();
                carAttribute.setCar(car);
                carAttribute.setAttribute(attribute);
                carAttribute.setValue(attrRequest.value());
                carAttributes.add(carAttribute);
            }
        }

        if (!carAttributes.isEmpty()) {
            carAttributeRepository.saveAll(carAttributes);
        }

        Car updatedCar = carRepository.findById(car.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        return CarMapper.INSTANCE.toCarDetailsResponse(updatedCar);
    }

    @Override
    public GlobalResponseDTO<?, CarDetailsResponseDTO> getCarById(Integer id) {
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Endpoint.V1.CAR.CAR_NOT_FOUND));
            CarDetailsResponseDTO carDetailsResponseDTO = CarMapper.INSTANCE.toCarDetailsResponse(car).withThumbnail(car.getThumbnail() != null ? car.getThumbnail() : null);
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Lấy thông tin xe thành công")
                            .build())
                    .data(carDetailsResponseDTO)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi lấy thông tin xe: " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<?, List<CarDetailsResponseDTO>> getAllCars() {
        try {
            List<CarDetailsResponseDTO> cars = carRepository.findAll().stream()
                    .map(CarMapper.INSTANCE::toCarDetailsResponse)
                    .collect(Collectors.toList());

            return GlobalResponseDTO.<NoPaginatedMeta, List<CarDetailsResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Lấy danh sách xe thành công")
                            .build())
                    .data(cars)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarDetailsResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi lấy danh sách xe: " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }
    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarResponseDTO>> getAllCarsPaginated(int page) {
        // Mỗi trang 12 phần tử, sắp xếp theo carId giảm dần (xe mới nhất trước)
        Pageable pageable = PageRequest.of(page, 12, Sort.by("carId").ascending());
        Page<Car> carPage = carRepository.findAll(pageable);

        // Map sang DTO
        List<CarResponseDTO> carDTOs = carPage.getContent().stream()
                .map(CarMapper.INSTANCE::toCarResponseDTO)
                .collect(Collectors.toList());
        // Thêm base URL vào từng ảnh
        List<CarResponseDTO> finalCarDTO = carDTOs
                .stream()
                .map(car -> {
                    if(car.thumbnail() == null) {
                        return car.withThumbnail(null);
                    }
                    return car.withThumbnail(car.thumbnail());
                })
                .toList();
        // Xây dựng thông tin phân trang
        Pagination pagination = Pagination.builder()
                .pageIndex(carPage.getNumber())
                .pageSize((short) carPage.getSize())
                .totalItems(carPage.getTotalElements())
                .totalPages(carPage.getTotalPages())
                .build();
        return GlobalResponseDTO.<PaginatedMeta, List<CarResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách xe thành công")
                        .pagination(pagination)
                        .build())
                .data(finalCarDTO)
                .build();
    }
    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarResponseDTO>> getAllCarsPaginated(FilterCarPaginationRequestDTO filter) {
        Sort sort = Sort.by(
                filter.direction().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.fields()
        );
        PageRequest pageRequest = PageRequest.of(
                filter.index(),
                filter.limit(),
                sort
        );
        // Tìm kiếm với specification
        Page<Car> carPage = carRepository.findAll(CarSpecification.withFilters(filter), pageRequest);

        // Map sang DTO
        List<CarResponseDTO> carDTOS = carPage.getContent().stream()
                .map(car -> {
                    CarResponseDTO carResponseDTO = CarMapper.INSTANCE.toCarResponseDTO(car);
                    return car.getThumbnail() != null ?
                        carResponseDTO.withThumbnail( car.getThumbnail()) :
                        carResponseDTO.withThumbnail(null);
                })
                .toList();
        // Xây dựng thông tin phân trang
        Pagination pagination = Pagination.builder()
                .pageIndex(carPage.getNumber())
                .pageSize((short) carPage.getSize())
                .totalItems(carPage.getTotalElements())
                .totalPages(carPage.getTotalPages())
                .build();
        return GlobalResponseDTO.<PaginatedMeta, List<CarResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách xe thành công")
                        .pagination(pagination)
                        .build())
                .data(carDTOS)
                .build();

    }
    @Override
    @Transactional
    public GlobalResponseDTO<?, CarDetailsResponseDTO> updateCar(Integer carId, UpdateCarRequestDTO carRequest, List<MultipartFile> newImages) {
        try {
            CarDetailsResponseDTO response = updateCarInternal(carId, carRequest, newImages);
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Cập nhật xe thành công")
                            .build())
                    .data(response)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, CarDetailsResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi cập nhật xe: " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }
    private CarDetailsResponseDTO updateCarInternal(Integer carId, UpdateCarRequestDTO carRequest, List<MultipartFile> newImages) throws IOException, NoSuchAlgorithmException {

        // === 1. LẤY XE CẦN CẬP NHẬT ===
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy xe với ID: " + carId));

        // === 2. XÓA ẢNH CŨ KHỎI CLOUDINARY VÀ DB (NẾU CÓ) ===
        if (carRequest.publicIdsToDelete() != null && !carRequest.publicIdsToDelete().isEmpty()) {
            for (String publicIdToDelete : carRequest.publicIdsToDelete()) {
                try {
                    // Xóa khỏi Cloudinary
                    ApiResponse destroyResponse = (ApiResponse) cloudinary.uploader().destroy(publicIdToDelete, ObjectUtils.emptyMap());
                    if ("ok".equals(destroyResponse.get("result"))) {
                        // Nếu xóa trên cloud thành công, xóa trong DB
                        // Bạn cần một phương thức để xóa Image bằng publicId (tên file)
                        imageRepository.deleteByUrl(publicIdToDelete + ".jpg");
                    } else {
                        // log.warn("Không thể xóa ảnh trên Cloudinary, publicId: {}", publicIdToDelete);
                    }
                } catch (Exception e) {
                    // log.error("Lỗi khi xóa ảnh với publicId: {}", publicIdToDelete, e);
                }
            }
        }
        // Cập nhật thông tin cơ bản của Car
        if (carRequest.name() != null) car.setName(carRequest.name());
        if (carRequest.model() != null) car.setModel(carRequest.model());
        if (carRequest.year() != null) car.setYear(carRequest.year());
        if (carRequest.price() != null) car.setPrice(carRequest.price());
        if (carRequest.manufacturerId() != null) car.setManufacturerId(carRequest.manufacturerId());
        if (carRequest.segmentId() != null) car.setSegmentId(carRequest.segmentId());

        // Xử lý Images
        List<Image> newlyUploadedImageEntities = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                // Giữ nguyên logic upload và tạo publicId/fileName của bạn
                String publicId = "car_" + car.getCarId() + "_" + System.currentTimeMillis();
                String fileName = publicId;
                    // Upload ảnh mới lên Cloudinary

                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                            "public_id", publicId, // Dùng cùng public_id
                            "folder", "car_images",
                            "width", TARGET_WIDTH,
                            "height", TARGET_HEIGHT,
                            "crop", "pad",
                            "background", "black"
                    ));
                    String imageUrl = (String) uploadResult.get("public_id");
                    Image newImage = new Image();
                    newImage.setCar(car);
                    newImage.setUrl(imageUrl); // Lưu fileName (cũng là publicId + .jpg)
                    newImage.setFileHash(calculateFileHash(file));
                    newlyUploadedImageEntities.add(newImage);
                }
            imageRepository.saveAll(newlyUploadedImageEntities);
            }
        NewThumbnailInfo newThumbnail = carRequest.newThumbnail();
        if (newThumbnail != null) {
            if ("EXISTING".equalsIgnoreCase(newThumbnail.type())) {
                // value bây giờ là fileName (url) của ảnh cũ
                car.setThumbnail(newThumbnail.value());
            } else if ("NEW".equalsIgnoreCase(newThumbnail.type())) {
                try {
                    // value là index của file mới trong mảng newImages
                    int newImageIndex = Integer.parseInt(newThumbnail.value());
                    if (newImageIndex >= 0 && newImageIndex < newlyUploadedImageEntities.size()) {
                        car.setThumbnail(newlyUploadedImageEntities.get(newImageIndex).getUrl());
                    } else {
                        throw new IllegalArgumentException("Index của thumbnail mới không hợp lệ.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Giá trị thumbnail mới không phải là một số.");
                }
            }
        }
        CarMapper.INSTANCE.updateCarFromDto(carRequest, car);
        // Xử lý CarAttribute
        if (carRequest.specifications() != null && !carRequest.specifications().isEmpty()) {
            List<CarAttribute> existingAttributes = carAttributeRepository.findByCarCarId(carId);
            Map<String, CarAttribute> existingAttrMap = existingAttributes.stream()
                    .collect(Collectors.toMap(
                            ca -> ca.getAttribute().getName() + "_" + ca.getAttribute().getSpecification().getSpecificationId(),
                            ca -> ca,
                            (ca1, ca2) -> ca1 // Giữ lại bản ghi đầu tiên nếu trùng key
                    ));

            List<CarAttribute> carAttributesToSave = new ArrayList<>();
            for (var specRequest : carRequest.specifications()) {
                Specification specification = specificationRepository.findByName(specRequest.name())
                        .orElseThrow(() -> new IllegalArgumentException("Specification not found: " + specRequest.name()));

                for (var attrRequest : specRequest.attributes()) {
                    Attribute attribute = attributeRepository.findByNameAndSpecificationSpecificationId(attrRequest.name(), specification.getSpecificationId())
                            .orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + attrRequest.name()));

                    String attrKey = attrRequest.name() + "_" + specification.getSpecificationId();
                    CarAttribute carAttribute = existingAttrMap.getOrDefault(attrKey, new CarAttribute());
                    carAttribute.setCar(car);
                    carAttribute.setAttribute(attribute);
                    if (attrRequest.value() != null) {
                        carAttribute.setValue(attrRequest.value());
                    }
                    carAttributesToSave.add(carAttribute);
                    existingAttrMap.remove(attrKey);
                }
            }

            // Xóa các attribute cũ không còn trong request
            if (!existingAttrMap.isEmpty()) {
                carAttributeRepository.deleteAll(existingAttrMap.values());
            }
            // Lưu các attribute mới hoặc cập nhật attribute cũ
            if (!carAttributesToSave.isEmpty()) {
                carAttributeRepository.saveAll(carAttributesToSave);
            }
        }

        Car updatedCar = carRepository.save(car);
        return CarMapper.INSTANCE.toCarDetailsResponse(updatedCar);
    }

    @Override
    @Transactional
    public GlobalResponseDTO<?, Void> deleteCar(Integer id) {
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Endpoint.V1.CAR.CAR_NOT_FOUND));
            carRepository.delete(car);

            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Xóa xe thành công")
                            .build())
                    .data(null)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Lỗi khi xóa xe: " + e.getMessage())
                            .build())
                    .data(null)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, List<CarDetailsResponseDTO>> importCarsFromJsonFile(MultipartFile jsonFile) throws IOException {
        if (jsonFile.isEmpty()) {
            throw new IllegalArgumentException("One or more files are empty");
        }
        if (jsonFile.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 20MB limit: " + jsonFile.getOriginalFilename());
        }
//        String contentType = jsonFile.getContentType();
//        if (!ALLOWED_TYPES.contains(contentType)) {
//            throw new IllegalArgumentException("Only JPEG, PNG, GIF files are allowed: " + jsonFile.getOriginalFilename());
//        }
        // Đọc nội dung file JSON
        String jsonContent = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);

        // Parse JSON thành danh sách AddCarRequestDTO
        ObjectMapper objectMapper = new ObjectMapper();
        List<AddCarRequestDTO> carRequests = objectMapper.readValue(
                jsonContent,
                new TypeReference<List<AddCarRequestDTO>>() {});

        // Xử lý từng request
        List<CarDetailsResponseDTO> responses = new ArrayList<>();
        for (AddCarRequestDTO carRequest : carRequests) {
            try {
                CarDetailsResponseDTO response = createCarV2Internal(carRequest);
                responses.add(response);
            } catch (Exception e) {
                // Ghi log lỗi và tiếp tục với các request khác
                log.error("Error importing car: " + e.getMessage(), e);
            }
        }

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarDetailsResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Imported " + responses.size() + " cars successfully")
                        .build())
                .data(responses)
                .build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedCarsByName(FindRelatedCarsRequestDTO requestDTO) {
        List<OptionProjection> relatedCars = carRepository.findRelatedCarsByNameNative(requestDTO.name());

        return GlobalResponseDTO.<NoPaginatedMeta, List<OptionProjection>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Find related cars successfully")
                        .build())
                .data(relatedCars)
                .build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<CarDetailsResponseDTO>> compareCars(CompareCarsRequestDTO compareCarsRequestDTO) {
        List<CarDetailsResponseDTO> carDetailsResponseDTOS = compareCarsRequestDTO.ids().stream()
                .map(id ->  CarMapper.INSTANCE.toCarDetailsResponse(carRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy xe có id = " + id))) )
                .toList();
        if(carDetailsResponseDTOS.isEmpty()){
            return GlobalResponseDTO.<NoPaginatedMeta, List<CarDetailsResponseDTO>>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.ERROR)
                            .message("Not found any cars")
                            .build())
                    .data(null)
                    .build();
        }
        return GlobalResponseDTO.<NoPaginatedMeta, List<CarDetailsResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Compare cars successfully")
                        .build())
                .data(carDetailsResponseDTOS)
                .build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedModelsByCarName(FindRelatedCarsRequestDTO requestDTO) {
        List<OptionProjection> relatedCars = carRepository.findRelatedModelsByCarName(requestDTO.name());

        return GlobalResponseDTO.<NoPaginatedMeta, List<OptionProjection>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Find related cars successfully")
                        .build())
                .data(relatedCars)
                .build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedCarNamesByCarName(FindRelatedCarsRequestDTO requestDTO) {
        List<OptionProjection> relatedCars = carRepository.findRelatedCarNamesByCarName(requestDTO.name());

        return GlobalResponseDTO.<NoPaginatedMeta, List<OptionProjection>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Find related cars name successfully")
                        .build())
                .data(relatedCars)
                .build();
    }

    private String calculateFileHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = file.getBytes();
        byte[] hashBytes = md.digest(fileBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
//    // Lấy danh sách ảnh của xe
//    public List<Image> getCarImages(Integer carId) {
//        return imageRepository.findByCarCarId(carId);
//    }
//
//    // Thêm ảnh cho xe
//    public Image addCarImage(Integer carId, String imageUrl) {
//        Car car = carRepository.findById(carId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CarConstants.CAR_NOT_FOUND));
//        Image image = new Image();
//        image.setCar(car);
//        image.setUrl(imageUrl);
//        return imageRepository.save(image);
//    }

    //    // Trong CarService.java
//    public CarAttribute createCarAttribute(Integer carId, Integer attributeId, String value) {
//        Car car = carRepository.findById(carId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CarConstants.CAR_NOT_FOUND));
//        Attribute attribute = attributeRepository.findById(attributeId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribute not found"));
//        CarAttribute carAttribute = new CarAttribute();
//        carAttribute.setCar(car);
//        carAttribute.setAttribute(attribute);
//        carAttribute.setValue(value);
//        return carAttributeRepository.save(carAttribute);
//    }
//
//    public CarAttribute updateCarAttribute(Integer carId, Integer attributeId, String value) {
//        CarAttribute carAttribute = carAttributeRepository.findById(new CarAttributeId(carId, attributeId))
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CarAttribute not found"));
//        carAttribute.setValue(value);
//        return carAttributeRepository.save(carAttribute);
//    }
//
//    public void deleteCarAttribute(Integer carId, Integer attributeId) {
//        CarAttribute carAttribute = carAttributeRepository.findById(new CarAttributeId(carId, attributeId))
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CarAttribute not found"));
//        carAttributeRepository.delete(carAttribute);
//    }

    //    @Transactional
//    public CarResponseDTO createCar(AddCarRequestDTO carRequest) {
//        // Tạo DTO mới với giá đã xử lý
//        AddCarRequestDTO adjustedRequest = new AddCarRequestDTO(
//                carRequest.manufacturerId(),
//                carRequest.segmentId(),
//                carRequest.name(),
//                carRequest.model(),
//                carRequest.year(),
//                    carRequest.price(),
//                carRequest.thumbnail(),
//                carRequest.specifications()
//        );
//
//        // Chuyển DTO thành entity Car và lưu
//        Car car = CarMapper.INSTANCE.toCar(adjustedRequest);
//        car = carRepository.save(car);
//
//        // Tạo danh sách CarAttribute
//        List<CarAttribute> carAttributes = new ArrayList<>();
//        for (var specRequest : adjustedRequest.specifications()) {
//            // Tìm hoặc tạo Specification
//            Specification specification = (Specification) specificationRepository.findByName(specRequest.name())
//                    .orElseGet(() -> {
//                        Specification newSpec = new Specification();
//                        newSpec.setName(specRequest.name());
//                        return specificationRepository.save(newSpec);
//                    });
//
//            for (var attrRequest : specRequest.attributes()) {
//                // Tìm hoặc tạo Attribute
//                Attribute attribute = attributeRepository.findByNameAndSpecificationSpecificationId(
//                                attrRequest.name(), specification.getSpecificationId())
//                        .orElseGet(() -> {
//                            Attribute newAttr = new Attribute();
//                            newAttr.setSpecification(specification);
//                            newAttr.setName(attrRequest.name());
//                            return attributeRepository.save(newAttr);
//                        });
//
//                // Kiểm tra xem CarAttribute đã tồn tại chưa
//                CarAttribute existingCarAttribute = carAttributeRepository.findByCarAndAttribute(car, attribute);
//                if (existingCarAttribute != null) {
//                    // Nếu đã tồn tại, cập nhật giá trị
//                    existingCarAttribute.setValue(attrRequest.value());
//                    carAttributes.add(existingCarAttribute);
//                } else {
//                    // Nếu chưa tồn tại, tạo mới
//                    CarAttribute carAttribute = new CarAttribute();
//                    carAttribute.setCar(car);
//                    carAttribute.setAttribute(attribute);
//                    carAttribute.setValue(attrRequest.value());
//                    carAttributes.add(carAttribute);
//                }
//            }
//        }
//
//        // Lưu tất cả CarAttribute (chỉ lưu những cái mới hoặc đã cập nhật)
//        if (!carAttributes.isEmpty()) {
//            carAttributeRepository.saveAll(carAttributes);
//        }
//
//        // Lấy lại Car từ database để đảm bảo dữ liệu đồng bộ
//        Car updatedCar = carRepository.findById(car.getCarId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
//        return CarMapper.INSTANCE.toCarResponse(updatedCar);
//    }
}
