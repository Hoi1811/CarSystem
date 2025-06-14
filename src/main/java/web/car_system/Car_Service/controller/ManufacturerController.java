package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerCreateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerUpdateDTO;
import web.car_system.Car_Service.service.ManufacturerService;

import java.io.IOException;

@RestApiV1
@Validated
@RequiredArgsConstructor
public class ManufacturerController {
    private final ManufacturerService manufacturerService;

    @GetMapping(Endpoint.V1.CAR.MANUFACTURER)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllManufacturers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        if (page < 0) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Số trang không được nhỏ hơn 0")
                                    .build())
                            .data(null)
                            .build());
        }

        if (size <= 0 || size > 100) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Kích thước trang phải từ 1 đến 100")
                                    .build())
                            .data(null)
                            .build());
        }

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            return ResponseEntity.ok(manufacturerService.getAllManufacturers(pageable));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Lỗi khi lấy danh sách hãng sản xuất: " + e.getMessage())
                                    .build())
                            .data(null)
                            .build());
        }
    }

    @PostMapping(Endpoint.V1.CAR.MANUFACTURER)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createManufacturer(
            @Valid @RequestPart("name") String name,
            @RequestPart("thumbnailFile") MultipartFile thumbnailFile) throws IOException {

        ManufacturerCreateDTO createDTO = new ManufacturerCreateDTO(name, thumbnailFile);
        return ResponseEntity.ok(manufacturerService.createManufacturer(createDTO));
    }

    @PutMapping(Endpoint.V1.CAR.MANUFACTURER_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> updateManufacturer(
            @PathVariable Integer id,
            @Valid @RequestPart("name") String name,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile) throws IOException {

        ManufacturerUpdateDTO updateDTO = new ManufacturerUpdateDTO(name, thumbnailFile);
        return ResponseEntity.ok(manufacturerService.updateManufacturer(id, updateDTO));
    }

    @DeleteMapping(Endpoint.V1.CAR.MANUFACTURER_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> deleteManufacturer(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(manufacturerService.deleteManufacturer(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message(e.getMessage())
                                    .build())
                            .data(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Lỗi khi xóa hãng sản xuất")
                                    .build())
                            .data(null)
                            .build());
        }
    }
}