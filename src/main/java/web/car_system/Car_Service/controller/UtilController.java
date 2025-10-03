package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.control_type.ControlTypeDTO;
import web.car_system.Car_Service.domain.dto.control_type.ControlTypeRelationDTO;
import web.car_system.Car_Service.domain.dto.control_type.SimpleComparisonRuleDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.entity.ComparisonRule;
import web.car_system.Car_Service.domain.entity.ControlType;
import web.car_system.Car_Service.repository.ComparisonRuleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@RestApiV1
@RequiredArgsConstructor
public class UtilController {
    private final ComparisonRuleRepository comparisonRuleRepository;
    @GetMapping(Endpoint.V1.UTIL.GET_CONTROL_TYPES) // Endpoint mới
    public ResponseEntity<GlobalResponseDTO<?, List<ControlTypeRelationDTO>>> getControlTypesWithRelations() {

        // 1. Lấy tất cả các rule từ DB và đưa vào Map để tra cứu nhanh
        Map<String, SimpleComparisonRuleDTO> allRulesMap = comparisonRuleRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ComparisonRule::getCode,
                        rule -> new SimpleComparisonRuleDTO(rule.getId(), rule.getCode())
                ));

        // 2. Xây dựng danh sách response
        List<ControlTypeRelationDTO> responseData = Arrays.stream(ControlType.values())
                .map(controlType -> {
                    // Lấy danh sách code của các rule tương thích (logic này vẫn cần có ở BE)
                    List<String> compatibleRuleCodes = getCompatibleRuleCodesFor(controlType);

                    // Map từ code sang DTO đầy đủ
                    List<SimpleComparisonRuleDTO> compatibleRules = compatibleRuleCodes.stream()
                            .map(allRulesMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    return new ControlTypeRelationDTO(controlType.name(), controlType.getDisplayName(), compatibleRules);
                })
                .collect(Collectors.toList());

        GlobalResponseDTO<?, List<ControlTypeRelationDTO>> response = GlobalResponseDTO.<NoPaginatedMeta, List<ControlTypeRelationDTO>>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy danh sách loại điều khiển thành công.").build())
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }
    // Hàm helper private để định nghĩa mối quan hệ ở một nơi duy nhất
    private List<String> getCompatibleRuleCodesFor(ControlType controlType) {
        switch (controlType) {
            case TEXT_INPUT: return List.of("none");
            case NUMBER_INPUT: return List.of("higher_is_better", "lower_is_better", "none");
            case SINGLE_SELECT: return List.of("enum_order", "none");
            case BOOLEAN_SELECT: return List.of("boolean_true_better", "none");
            case POWER_TORQUE_INPUT: //
            case DIMENSION_INPUT:    // fall-through
            default: return List.of("none");
        }
    }
}
