package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.car.*;
import web.car_system.Car_Service.domain.dto.global.FilterCarPaginationRequestDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.image.CarImagesResponseDTO;
import web.car_system.Car_Service.domain.entity.Attribute;
import web.car_system.Car_Service.domain.entity.Image;
import web.car_system.Car_Service.domain.entity.Specification;
import web.car_system.Car_Service.service.AttributeService;
import web.car_system.Car_Service.service.ImageService;
import web.car_system.Car_Service.service.SpecificationService;
import web.car_system.Car_Service.service.impl.CarServiceImpl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestApiV1
public class CarController {
    private final CarServiceImpl carService;
    private final ImageService imageService;
    private final SpecificationService specificationService;
    private final AttributeService attributeService;

    @PostMapping(value = Endpoint.V1.CAR.CAR_IMPORT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> importCars(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            if (!file.getContentType().equals("application/json")) {
                throw new IllegalArgumentException("Only JSON files are allowed");
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(carService.importCarsFromJsonFile(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Error importing cars: " + e.getMessage())
                                    .build())
                            .data(null)
                            .build());
        }
    }

    @PostMapping(value = Endpoint.V1.CAR.CAR_IMPORT_BATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> importMultipleCars(
            @RequestParam("files") MultipartFile[] files) {

        List<GlobalResponseDTO<?, ?>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                GlobalResponseDTO<?, ?> response = carService.importCarsFromJsonFile(file);
                results.add(response);
            } catch (Exception e) {
                results.add(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message("Error processing file " + file.getOriginalFilename() + ": " + e.getMessage())
                                .build())
                        .data(null)
                        .build());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalResponseDTO.<NoPaginatedMeta, List<GlobalResponseDTO<?, ?>>>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.SUCCESS)
                                .message("Processed " + results.size() + " files")
                                .build())
                        .data(results)
                        .build());
    }
    @PostMapping(Endpoint.V1.CAR.FIND_RELATED_CARS_BY_NAME)
    public ResponseEntity<GlobalResponseDTO<?,?>> findRelatedCarsByName(@RequestBody FindRelatedCarsRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(carService.findRelatedCarsByName(requestDTO));
    }
    @PostMapping(Endpoint.V1.CAR.FIND_RELATED_MODELS_BY_NAME)
    public ResponseEntity<GlobalResponseDTO<?,?>> findRelatedModelsByName(@RequestBody FindRelatedCarsRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(carService.findRelatedModelsByCarName(requestDTO));
    }
    @PostMapping(Endpoint.V1.CAR.FIND_RELATED_CAR_NAMES_BY_NAME)
    public ResponseEntity<GlobalResponseDTO<?,?>> findRelatedCarNamesByName(@RequestBody FindRelatedCarsRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(carService.findRelatedCarNamesByCarName(requestDTO));
    }
    @PostMapping(Endpoint.V1.CAR.COMPARE_CARS)
    public ResponseEntity<GlobalResponseDTO<?,?>> compareCars(@RequestBody CompareCarsRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(carService.compareCars(requestDTO));
    }
    @PostMapping(Endpoint.V1.CAR.CAR)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createCar(@RequestBody AddCarRequestDTO carRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCar(carRequest));
    }

    @PostMapping(value = Endpoint.V1.CAR.CAR_V2, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createCarV2(
            @RequestPart("carRequest") AddCarRequestDTO carRequest,
            @RequestPart("images") MultipartFile[] images,
            @RequestParam("thumbnailIndex") Integer thumbnailIndex)
            throws IOException, NoSuchAlgorithmException {

        if (images == null || images.length == 0) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Required part 'images' is not present")
                                    .build())
                            .data(null)
                            .build());
        }

        AddCarRequestDTO adjustedRequest = new AddCarRequestDTO(
                carRequest.manufacturerId(),
                carRequest.segmentId(),
                carRequest.name(),
                carRequest.model(),
                carRequest.year(),
                carRequest.price(),
                Arrays.asList(images),
                thumbnailIndex,
                carRequest.carTypeIds(),
                carRequest.origin(),
                carRequest.specifications()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCarV2(adjustedRequest));
    }

    @PostMapping(value = Endpoint.V1.CAR.CAR_V3, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createCarV3(
            @RequestPart("carRequest") AddCarRequestDTO carRequest,
            @RequestPart("images") MultipartFile[] images,
            @RequestParam("thumbnailIndex") Integer thumbnailIndex)
            throws Throwable {

        if (images == null || images.length == 0) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Required part 'images' is not present")
                                    .build())
                            .data(null)
                            .build());
        }

        AddCarRequestDTO adjustedRequest = new AddCarRequestDTO(
                carRequest.manufacturerId(),
                carRequest.segmentId(),
                carRequest.name(),
                carRequest.model(),
                carRequest.year(),
                carRequest.price(),
                Arrays.asList(images),
                thumbnailIndex,
                carRequest.carTypeIds(),
                carRequest.origin(),
                carRequest.specifications()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCarV3(adjustedRequest));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getCarById(@PathVariable Integer id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @GetMapping(Endpoint.V1.CAR.CAR)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @PostMapping(Endpoint.V1.CAR.CAR_PAGINATED)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllCarsPaginated(
            @RequestBody @Valid FilterCarPaginationRequestDTO filter) {
        return ResponseEntity.ok(carService.getAllCarsPaginated(filter));
    }

    @PutMapping(value = Endpoint.V1.CAR.CAR_ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> updateCar(
            @PathVariable Integer id,
            @RequestPart("updateRequest") UpdateCarRequestDTO updateRequest, // <-- Dùng DTO mới
            @RequestPart(name = "newImages", required = false) List<MultipartFile> newImages) {
        GlobalResponseDTO<?, CarDetailsResponseDTO> response = carService.updateCar(id, updateRequest, newImages);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> deleteCar(@PathVariable Integer id) {
        return ResponseEntity.ok(carService.deleteCar(id));
    }

    @PostMapping(Endpoint.V1.CAR.CAR_ID_THUMBNAIL)
    public ResponseEntity<GlobalResponseDTO<?, ?>> uploadCarThumbnail(
            @PathVariable Integer carId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.SUCCESS)
                                .message("Upload thumbnail thành công")
                                .build())
                        .data(imageService.uploadCarThumbnail(carId, file))
                        .build()
        );
    }

    @PostMapping(Endpoint.V1.CAR.CAR_ID_IMAGES)
    public ResponseEntity<List<Image>> uploadImages(@PathVariable Integer carId,
                                                    @RequestParam("files") MultipartFile[] files)
            throws IOException, IllegalArgumentException {
        List<Image> images = imageService.uploadImages(carId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(images);
    }

    @GetMapping(Endpoint.V1.CAR.CAR_ID_IMAGES)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>>> getImagesByCarId(@PathVariable Integer carId) {
        GlobalResponseDTO<NoPaginatedMeta, List<CarImagesResponseDTO>> response = imageService.getImagesByCarId(carId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Endpoint.V1.CAR.CAR_SPECIFICATIONS)
    public ResponseEntity<Specification> createSpecification(@RequestBody String name) {
        Specification specification = specificationService.createSpecification(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(specification);
    }

    @PutMapping(Endpoint.V1.CAR.CAR_SPECIFICATION_ID)
    public ResponseEntity<Specification> updateSpecification(@PathVariable Integer id, @RequestBody String name) {
        Specification specification = specificationService.updateSpecification(id, name);
        return ResponseEntity.ok(specification);
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_SPECIFICATION_ID)
    public ResponseEntity<Void> deleteSpecification(@PathVariable Integer id) {
        specificationService.deleteSpecification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Endpoint.V1.CAR.CAR_SPECIFICATION_ATTRIBUTES)
    public ResponseEntity<Attribute> createAttribute(@PathVariable Integer specId, @RequestBody String name) {
        Attribute attribute = attributeService.createAttribute(specId, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(attribute);
    }

    @PutMapping(Endpoint.V1.CAR.CAR_ATTRIBUTE_ID)
    public ResponseEntity<Attribute> updateAttribute(@PathVariable Integer id, @RequestBody String name) {
        Attribute attribute = attributeService.updateAttribute(id, name);
        return ResponseEntity.ok(attribute);
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_ATTRIBUTE_ID)
    public ResponseEntity<Void> deleteAttribute(@PathVariable Integer id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }
}