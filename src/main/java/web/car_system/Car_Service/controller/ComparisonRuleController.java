package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.entity.ComparisonRule;
import web.car_system.Car_Service.service.ComparisonRuleService; // <-- Import Service

import java.util.List;

@RestApiV1
@RequiredArgsConstructor
public class ComparisonRuleController {

    // --- THAY ĐỔI CHÍNH: Inject Service thay vì Repository ---
    private final ComparisonRuleService comparisonRuleService;

    @GetMapping(Endpoint.V1.COMPARISON_RULE.GET_ALL)
    public ResponseEntity<GlobalResponseDTO<?, List<ComparisonRule>>> getAllComparisonRules() {

        // --- THAY ĐỔI CHÍNH: Gọi phương thức từ Service ---
        List<ComparisonRule> rules = comparisonRuleService.getAllRules();

        // Phần logic gói response giữ nguyên
        GlobalResponseDTO<?, List<ComparisonRule>> response = GlobalResponseDTO.<NoPaginatedMeta, List<ComparisonRule>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách luật so sánh thành công")
                        .build())
                .data(rules)
                .build();

        return ResponseEntity.ok(response);
    }
}