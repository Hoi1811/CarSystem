package web.car_system.Car_Service.domain.dto.attribute;

import lombok.Builder;
import web.car_system.Car_Service.domain.entity.Attribute;

@Builder
public record AttributeResponseDTO(
        Integer attributeId,
        String name,
        String description,
        String controlType,
        String optionsSource, // Giữ lại trường này
        String unit,
        Float weight,
        SpecificationInfo specification, // Nhúng thông tin của Specification
        ComparisonRuleInfo comparisonRule // Nhúng thông tin của ComparisonRule
) {
    // Nhúng các DTO con để làm cho response có cấu trúc
    public record SpecificationInfo(Integer id, String name) {}
    public record ComparisonRuleInfo(Integer id, String code, String description) {}

    // Phương thức factory để chuyển đổi từ Entity sang DTO
    public static AttributeResponseDTO fromEntity(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        SpecificationInfo specInfo = new SpecificationInfo(
                attribute.getSpecification().getSpecificationId(),
                attribute.getSpecification().getName()
        );

        ComparisonRuleInfo ruleInfo = new ComparisonRuleInfo(
                attribute.getComparisonRule().getId(),
                attribute.getComparisonRule().getCode(),
                attribute.getComparisonRule().getDescription()
        );

        return new AttributeResponseDTO(
                attribute.getAttributeId(),
                attribute.getName(),
                attribute.getDescription(),
                attribute.getControlType(),
                attribute.getOptionsSource(),
                attribute.getUnit(),
                attribute.getWeight(),
                specInfo,
                ruleInfo
        );
    }
}