package web.car_system.Car_Service.domain.dto.specification;

import web.car_system.Car_Service.domain.dto.attribute.AttributeResponseDTO;

import java.util.List;

public record SpecificationResponseDTO(
        Integer id,
        String name,
        List<AttributeResponseDTO> attributes
) {
}
