package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.dto.recommendation.CreateOrUpdateRuleRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRuleDto;

import java.util.List;

public interface RecommendationService {

    // === Nghiệp vụ cho Khách hàng ===

    /**
     * Tìm kiếm và trả về danh sách xe được gợi ý dựa trên các tiêu chí của người dùng.
     * @param request Chứa một map các tiêu chí.
     * @return Danh sách các xe (InventoryCarDto) phù hợp.
     */
    List<CarResponseDTO> findSuggestions(RecommendationRequest request);


    // === Nghiệp vụ cho Admin (CRUD các Rule) ===

    List<RecommendationRuleDto> getAllRules();

    RecommendationRuleDto getRuleById(Long id);

    RecommendationRuleDto createRule(CreateOrUpdateRuleRequest request);

    RecommendationRuleDto updateRule(Long id, CreateOrUpdateRuleRequest request);

    void deleteRule(Long id);
}