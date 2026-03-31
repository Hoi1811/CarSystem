package web.car_system.Car_Service.domain.dto.specification;

import web.car_system.Car_Service.domain.dto.attribute.AddAttributeRequestDTO;

import java.util.List;

public record AddSpecificationRequestDTO(
        Integer id,
        String name,
        List<AddAttributeRequestDTO> attributes
) {
}
