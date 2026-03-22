package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.comparison.ComparisonRuleDto;
import web.car_system.Car_Service.domain.entity.ComparisonRule;
import web.car_system.Car_Service.domain.entity.ControlType;
import web.car_system.Car_Service.repository.ComparisonRuleRepository;
import web.car_system.Car_Service.service.ComparisonRuleService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComparisonRuleServiceImpl implements ComparisonRuleService {

    private final ComparisonRuleRepository comparisonRuleRepository;

    @Override
    public List<ComparisonRuleDto> getAllRules() {
        return comparisonRuleRepository.findAll().stream()
                .map(r -> new ComparisonRuleDto(r.getId(), r.getCode(), r.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCompatibleRuleCodesFor(ControlType controlType) {
        switch (controlType) {
            case TEXT_INPUT:         return List.of("none");
            case NUMBER_INPUT:       return List.of("higher_is_better", "lower_is_better", "none");
            case SINGLE_SELECT:      return List.of("enum_order", "none");
            case BOOLEAN_SELECT:     return List.of("boolean_true_better", "none");
            case POWER_TORQUE_INPUT: // fall-through
            case DIMENSION_INPUT:    // fall-through
            default:                 return List.of("none");
        }
    }
}