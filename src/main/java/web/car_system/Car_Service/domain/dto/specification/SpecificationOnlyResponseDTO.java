package web.car_system.Car_Service.domain.dto.specification;

import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;

import java.util.List;

public record SpecificationOnlyResponseDTO(
        Integer id,
        String name,
        List<AttributeOnlyResponseDTO> attributes
) {
}
