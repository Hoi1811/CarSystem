package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.comparison.ComparisonRuleDto;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.service.ComparisonRuleService;

import java.util.List;

import static web.car_system.Car_Service.utility.ResponseFactory.success;

@RestApiV1
@RequiredArgsConstructor
public class ComparisonRuleController {

    private final ComparisonRuleService comparisonRuleService;

    @GetMapping(Endpoint.V1.COMPARISON_RULE.GET_ALL)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<ComparisonRuleDto>>> getAllComparisonRules() {
        List<ComparisonRuleDto> rules = comparisonRuleService.getAllRules();
        return success(rules, "Lấy danh sách luật so sánh thành công");
    }
}