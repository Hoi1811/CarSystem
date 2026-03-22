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
import web.car_system.Car_Service.service.ComparisonRuleService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@RestApiV1
@RequiredArgsConstructor
public class UtilController {

    private final ComparisonRuleService comparisonRuleService;

    @GetMapping(Endpoint.V1.UTIL.GET_CONTROL_TYPES)
    public ResponseEntity<GlobalResponseDTO<?, List<ControlTypeRelationDTO>>> getControlTypesWithRelations() {

        // 1. Lấy tất cả các rule từ Service và đưa vào Map để tra cứu nhanh
        Map<String, SimpleComparisonRuleDTO> allRulesMap = comparisonRuleService.getAllRules().stream()
                .collect(Collectors.toMap(
                        r -> r.getCode(),
                        r -> new SimpleComparisonRuleDTO(r.getId(), r.getCode())
                ));

        // 2. Xây dựng danh sách response
        List<ControlTypeRelationDTO> responseData = Arrays.stream(ControlType.values())
                .map(controlType -> {
                    List<String> compatibleRuleCodes = comparisonRuleService.getCompatibleRuleCodesFor(controlType);

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
}
