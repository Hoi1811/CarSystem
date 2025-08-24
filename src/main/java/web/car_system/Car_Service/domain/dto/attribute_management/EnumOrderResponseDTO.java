package web.car_system.Car_Service.domain.dto.attribute_management;

import lombok.Builder;
import web.car_system.Car_Service.domain.entity.AttributeEnumOrder;

@Builder
public record EnumOrderResponseDTO(
        Integer attributeId,
        String valueKey,
        String displayValue,
        Integer rank
) {
    public static EnumOrderResponseDTO fromEntity(AttributeEnumOrder entity) {
        return new EnumOrderResponseDTO(
                entity.getAttribute().getAttributeId(),
                entity.getId().getValueKey(),
                entity.getDisplayValue(),
                entity.getRank()
        );
    }
}