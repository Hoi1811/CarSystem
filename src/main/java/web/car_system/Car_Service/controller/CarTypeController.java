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
import web.car_system.Car_Service.domain.dto.car_type.CarTypeCreateDTO;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeUpdateDTO;
import web.car_system.Car_Service.service.CarTypeService;

import java.io.IOException;

@RestApiV1
@Validated
@RequiredArgsConstructor
public class CarTypeController {
    private final CarTypeService carTypeService;

    @PostMapping(Endpoint.V1.CAR.CAR_TYPE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createCarType(
            @Valid @RequestPart("name") String name,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("thumbnailFile") MultipartFile thumbnailFile) throws IOException {

        CarTypeCreateDTO createDTO = new CarTypeCreateDTO(name, description, thumbnailFile);
        return ResponseEntity.ok(carTypeService.createCarType(createDTO));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_TYPE_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getCarTypeById(
            @PathVariable Integer typeId) {
        return ResponseEntity.ok(carTypeService.getCarTypeById(typeId));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_TYPE)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllCarTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        if (page >= 0 && size > 0) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            return ResponseEntity.ok(carTypeService.getAllCarTypes(pageable));
        }
        return ResponseEntity.ok(carTypeService.getAllCarTypes());
    }

    @PutMapping(Endpoint.V1.CAR.CAR_TYPE_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> updateCarType(
            @PathVariable Integer typeId,
            @Valid @RequestPart("name") String name,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile) throws IOException {

        CarTypeUpdateDTO updateDTO = new CarTypeUpdateDTO(name, description, thumbnailFile);
        return ResponseEntity.ok(carTypeService.updateCarType(typeId, updateDTO));
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_TYPE_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> deleteCarType(
            @PathVariable Integer typeId) {
        return ResponseEntity.ok(carTypeService.deleteCarType(typeId));
    }
}