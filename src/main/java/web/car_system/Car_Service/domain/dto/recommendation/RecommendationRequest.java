package web.car_system.Car_Service.domain.dto.recommendation;

import lombok.Data;

import java.util.Map;

@Data
public class RecommendationRequest {
    // Sử dụng Map<String, Object> để linh hoạt
    // Key: "budget", "usage", "seats"
    // Value: "500-700", "family", 5
    private Map<String, Object> criteria;
}