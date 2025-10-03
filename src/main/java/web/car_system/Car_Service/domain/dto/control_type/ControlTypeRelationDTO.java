package web.car_system.Car_Service.domain.dto.control_type;

import java.util.List;

public record ControlTypeRelationDTO(
        String code,
        String name,
        List<SimpleComparisonRuleDTO> compatibleRules
) {}