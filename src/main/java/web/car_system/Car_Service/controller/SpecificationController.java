package web.car_system.Car_Service.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.attribute.AttributeSearchRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;
import web.car_system.Car_Service.domain.dto.specification.SpecificationOnlyResponseDTO;
import web.car_system.Car_Service.service.SpecificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SpecificationController {

    private final SpecificationService specificationService;

    @PostMapping(Endpoint.V1.CAR.SPECIFICATION_ATTRIBUTES)
    public GlobalResponseDTO<PaginatedMeta, List<AttributeOnlyResponseDTO>> getAttributesBySpecificationId(
            @Valid @RequestBody AttributeSearchRequestDTO request) {
        return specificationService.getAttributesBySpecificationId(request);
    }

    @GetMapping(Endpoint.V1.CAR.SPECIFICATIONS)
    public GlobalResponseDTO<NoPaginatedMeta, ?> getAllSpecifications() {
        return specificationService.findAllSpecificationsWithLimitedAttributes();
    }

    // --- ENDPOINT MỚI ĐỂ LẤY SCHEMA CHO FORM ---
    @GetMapping(Endpoint.V1.CAR.SPECIFICATIONS_SCHEMA)
    public GlobalResponseDTO<NoPaginatedMeta, ?> getSpecificationsFormSchema() {
        return specificationService.getFormSchema();
    }
}