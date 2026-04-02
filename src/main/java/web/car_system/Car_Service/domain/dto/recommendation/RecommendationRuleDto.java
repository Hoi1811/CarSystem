package web.car_system.Car_Service.domain.dto.recommendation;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@Builder
public class RecommendationRuleDto {
    private Long id;
    private String ruleName;
    private String description;

    // Sử dụng JsonNode (từ thư viện Jackson) để làm việc với JSON một cách linh hoạt
    private JsonNode conditionsJson;
    private JsonNode suggestionJson;

    @JsonProperty("isActive")
    private boolean isActive;
}