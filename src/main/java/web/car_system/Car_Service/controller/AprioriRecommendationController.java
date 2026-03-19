package web.car_system.Car_Service.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.car.CarSuggestionDto;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.AprioriRecommendationService;

import java.util.List;

/**
 * Controller for Apriori-based car recommendations
 * Public APIs for getting intelligent car suggestions
 */
@RestApiV1
@RequiredArgsConstructor
@Validated
public class AprioriRecommendationController {
    
    private final AprioriRecommendationService recommendationService;
    
    /**
     * Get recommendations for a specific car
     * "Customers who viewed this also viewed..."
     * 
     * GET /api/v1/apriori-recommendations/cars/{carId}
     */
    @GetMapping("/apriori-recommendations/cars/{carId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<CarSuggestionDto>>> getRecommendationsForCar(
            @PathVariable Integer carId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        
        List<CarSuggestionDto> recommendations = recommendationService.getRecommendationsForCar(carId, limit);
        
        return ResponseEntity.ok(
                GlobalResponseDTO.<NoPaginatedMeta, List<CarSuggestionDto>>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.SUCCESS)
                                .message("Lấy danh sách xe gợi ý thành công")
                                .build())
                        .data(recommendations)
                        .build()
        );
    }
    
    /**
     * Get personalized recommendations based on user history
     * "Recommended for you"
     * 
     * GET /api/v1/apriori-recommendations/personal
     */
    @GetMapping("/apriori-recommendations/personal")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<CarSuggestionDto>>> getPersonalizedRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            jakarta.servlet.http.HttpServletRequest request) {

        // Use session ID from request if not provided — getSession(false) avoids creating a new session
        if (sessionId == null) {
            jakarta.servlet.http.HttpSession existingSession = request.getSession(false);
            sessionId = (existingSession != null)
                    ? existingSession.getId()
                    : java.util.UUID.randomUUID().toString();
        }
        
        List<CarSuggestionDto> recommendations = recommendationService.getPersonalizedRecommendations(
                userId, sessionId, limit
        );
        
        return ResponseEntity.ok(
                GlobalResponseDTO.<NoPaginatedMeta, List<CarSuggestionDto>>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.SUCCESS)
                                .message("Lấy danh sách xe gợi ý cá nhân hóa thành công")
                                .build())
                        .data(recommendations)
                        .build()
        );
    }
    
    /**
     * Manual trigger for rule generation (Admin only - should add security)
     * POST /api/v1/apriori-recommendations/generate-rules
     */
    @PostMapping("/apriori-recommendations/generate-rules")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, String>> generateRules() {
        int rulesGenerated = recommendationService.generateAprioriRules();
        
        return ResponseEntity.ok(
                GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.SUCCESS)
                                .message("Tạo luật gợi ý thành công")
                                .build())
                        .data("Đã tạo " + rulesGenerated + " luật gợi ý từ thuật toán Apriori")
                        .build()
        );
    }
}
