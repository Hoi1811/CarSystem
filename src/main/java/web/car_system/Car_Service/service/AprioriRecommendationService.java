package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.car.CarSuggestionDto;

import java.util.List;

/**
 * Service for Apriori-based car recommendations
 * Separate from legacy RecommendationService
 */
public interface AprioriRecommendationService {
    
    /**
     * Get car recommendations based on Apriori association rules
     * 
     * @param carId ID of the car being viewed
     * @param limit Maximum number of recommendations
     * @return List of recommended cars
     */
    List<CarSuggestionDto> getRecommendationsForCar(Integer carId, int limit);
    
    /**
     * Get personalized recommendations based on user's viewing history
     * 
     * @param userId User ID (can be null for guests)
     * @param sessionId Session ID for guest users
     * @param limit Maximum number of recommendations
     * @return List of recommended cars
     */
    List<CarSuggestionDto> getPersonalizedRecommendations(Long userId, String sessionId, int limit);
    
    /**
     * Generate Apriori association rules from activity logs
     * Called by scheduled job
     * 
     * @return Number of rules generated
     */
    int generateAprioriRules();
}
