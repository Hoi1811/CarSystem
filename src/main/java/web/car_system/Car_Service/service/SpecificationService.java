package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.attribute.AttributeSearchRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;
import web.car_system.Car_Service.domain.dto.specification.SpecificationOnlyResponseDTO;
import web.car_system.Car_Service.domain.entity.Specification;

import java.util.List;

public interface SpecificationService {
    Specification createSpecification(String name);
    Specification updateSpecification(Integer id, String name);
    void deleteSpecification(Integer id);
    GlobalResponseDTO<NoPaginatedMeta, List<SpecificationOnlyResponseDTO>> findAllSpecificationsWithLimitedAttributes();
    GlobalResponseDTO<PaginatedMeta, List<AttributeOnlyResponseDTO>> getAttributesBySpecificationId(AttributeSearchRequestDTO request);
}
