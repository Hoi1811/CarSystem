package web.car_system.Car_Service.domain.dto.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;

import java.util.List;

public record SpecificationOnlyResponseDTO(
        @JsonProperty("specificationId")
        Integer id,
        @JsonProperty("specificationName")
        String name,
        @JsonProperty("attributes")
        List<AttributeOnlyResponseDTO> attributes
) {
}
