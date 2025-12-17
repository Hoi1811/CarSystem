package web.car_system.Car_Service.domain.dto.comparison;

import lombok.Builder;

@Builder
public record AttributeValueComparisonDTO(
        Integer carId,
        String displayValue, // Ví dụ: "155 hp", "Tự động 6 cấp", "Có"
        ComparisonOutcome outcome
) {}