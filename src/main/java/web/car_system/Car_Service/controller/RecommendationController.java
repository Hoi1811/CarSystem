package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.dto.recommendation.CreateOrUpdateRuleRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRuleDto;
import web.car_system.Car_Service.service.RecommendationService;

import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.RECOMMENDATION.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // === PUBLIC ENDPOINT ===

    @PostMapping(GET_SUGGESTIONS)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<CarResponseDTO>>> getSuggestions(
            @Valid @RequestBody RecommendationRequest request,
            Pageable pageable) {
        
        Page<CarResponseDTO> suggestions = recommendationService.findSuggestions(request, pageable);
        return successPageable(suggestions, "Lấy danh sách xe gợi ý thành công.");
    }

    // === ADMIN ENDPOINTS ===

    @GetMapping(GET_ALL_RULES)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<RecommendationRuleDto>>> getAllRules() {
        List<RecommendationRuleDto> rules = recommendationService.getAllRules();
        return success(rules, "Lấy danh sách quy tắc thành công.");
    }

    @GetMapping(GET_RULE_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RecommendationRuleDto>> getRuleById(@PathVariable Long id) {
        RecommendationRuleDto rule = recommendationService.getRuleById(id);
        return success(rule, "Lấy chi tiết quy tắc thành công.");
    }

    @PostMapping(CREATE_RULE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RecommendationRuleDto>> createRule(
            @Valid @RequestBody CreateOrUpdateRuleRequest request) {
        RecommendationRuleDto newRule = recommendationService.createRule(request);
        return success(newRule, "Tạo quy tắc mới thành công.", HttpStatus.CREATED);
    }

    @PutMapping(UPDATE_RULE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RecommendationRuleDto>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrUpdateRuleRequest request) {
        RecommendationRuleDto updatedRule = recommendationService.updateRule(id, request);
        return success(updatedRule, "Cập nhật quy tắc thành công.");
    }

    @DeleteMapping(DELETE_RULE)
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        recommendationService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
