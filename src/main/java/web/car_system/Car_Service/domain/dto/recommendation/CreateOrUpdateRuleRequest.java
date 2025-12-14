package web.car_system.Car_Service.domain.dto.recommendation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;


@Data
public class CreateOrUpdateRuleRequest {
    @NotEmpty
    private String ruleName;
    private String description;
    @NotNull
    private JsonNode conditionsJson;
    @NotNull
    private JsonNode suggestionJson;
    private boolean isActive = true;
}