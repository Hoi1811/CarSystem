package web.car_system.Car_Service.domain.dto.comparison;

import lombok.Builder;
import java.util.List;

@Builder
public record SpecificationComparisonDTO(
        String specificationName,
        List<AttributeComparisonDTO> attributeComparisons
) {}