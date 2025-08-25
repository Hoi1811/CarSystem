package web.car_system.Car_Service.domain.dto.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttributeOnlyResponseDTO(
        @JsonProperty("attributeId")
        Integer id,
        @JsonProperty("attributeName")
        String name,
        String controlType,
        String optionsSource
) {
}
