package web.car_system.Car_Service.domain.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import web.car_system.Car_Service.domain.dto.recommendation.CreateOrUpdateRuleRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRuleDto;
import web.car_system.Car_Service.domain.entity.RecommendationRule;

@Mapper(componentModel = "spring")
public abstract class RecommendationRuleMapper {
    // Tiêm ObjectMapper của Jackson để làm việc với JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Chuyển từ Entity sang DTO ---
    public RecommendationRuleDto toDto(RecommendationRule rule) {
        if (rule == null) {
            return null;
        }
        return RecommendationRuleDto.builder()
                .id(rule.getId())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .conditionsJson(toJsonNode(rule.getConditionsJson()))
                .suggestionJson(toJsonNode(rule.getSuggestionJson()))
                .isActive(rule.isActive())
                .build();
    }

    // --- Chuyển từ DTO (Request) sang Entity ---
    public RecommendationRule toEntity(CreateOrUpdateRuleRequest request) {
        if (request == null) {
            return null;
        }
        return RecommendationRule.builder()
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .conditionsJson(toString(request.getConditionsJson()))
                .suggestionJson(toString(request.getSuggestionJson()))
                .isActive(request.isActive())
                .build();
    }

    // Helper methods
    public JsonNode toJsonNode(String jsonString) {
        if (jsonString == null) return null;
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            // Có thể throw một exception custom ở đây
            throw new RuntimeException("Lỗi parse chuỗi JSON", e);
        }
    }

    public String toString(JsonNode jsonNode) {
        if (jsonNode == null) return null;
        return jsonNode.toString();
    }
}
