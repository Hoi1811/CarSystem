package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.car.CarSuggestionDto;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.repository.AprioriRuleRepository;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.UserActivityLogRepository;
import web.car_system.Car_Service.service.AprioriRecommendationService;
import web.car_system.Car_Service.util.AprioriEngine;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Apriori-based recommendation service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AprioriRecommendationServiceImpl implements AprioriRecommendationService {
    
    private final AprioriRuleRepository aprioriRuleRepository;
    private final UserActivityLogRepository activityLogRepository;
    private final CarRepository carRepository;
    private final AprioriEngine aprioriEngine;
    
    // Configuration (can be moved to application.yml)
    private static final double MIN_SUPPORT = 0.01;  // 1% of transactions
    private static final double MIN_CONFIDENCE = 0.3;  // 30% confidence
    private static final int LOOKBACK_DAYS = 30;  // Use last 30 days of data
    
    @Override
    @Transactional(readOnly = true)
    public List<CarSuggestionDto> getRecommendationsForCar(Integer carId, int limit) {
        log.debug("Getting recommendations for carId={}, limit={}", carId, limit);
        
        // Find rules where this car is the antecedent
        List<AprioriRule> rules = aprioriRuleRepository.findTopRecommendationsForCar(carId);
        
        if (rules.isEmpty()) {
            log.debug("No Apriori rules found for carId={}, returning empty list", carId);
            return Collections.emptyList();
        }
        
        // Convert to DTOs
        return rules.stream()
                .limit(limit)
                .map(rule -> convertToCarSuggestionDto(rule.getConsequentCar()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CarSuggestionDto> getPersonalizedRecommendations(Long userId, String sessionId, int limit) {
        log.debug("Getting personalized recommendations for userId={}, sessionId={}", userId, sessionId);
        
        // Get recent activities
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<UserActivityLog> recentActivities;
        
        if (userId != null) {
            recentActivities = activityLogRepository.findRecentUserActivities(
                    userId, ActivityType.VIEW_CAR, since
            );
        } else {
            recentActivities = activityLogRepository.findBySessionIdOrderByActivityTimestampAsc(sessionId);
        }
        
        if (recentActivities.isEmpty()) {
            log.debug("No recent activities found");
            return Collections.emptyList();
        }
        
        // Extract car IDs from activities
        Set<Integer> viewedCarIds = recentActivities.stream()
                .map(log -> log.getCar() != null ? log.getCar().getCarId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // Score recommendations based on all viewed cars
        Map<Integer, Double> carScores = new HashMap<>();
        
        for (Integer carId : viewedCarIds) {
            List<AprioriRule> rules = aprioriRuleRepository.findTopRecommendationsForCar(carId);
            for (AprioriRule rule : rules) {
                Integer recommendedCarId = rule.getConsequentCar().getCarId();
                
                // Skip if already viewed
                if (viewedCarIds.contains(recommendedCarId)) continue;
                
                // Add confidence score
                carScores.merge(recommendedCarId, rule.getConfidence(), Double::sum);
            }
        }
        
        // Sort by score and convert to DTOs
        return carScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> carRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::convertToCarSuggestionDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public int generateAprioriRules() {
        log.info("Starting Apriori rule generation...");
        
        LocalDateTime since = LocalDateTime.now().minusDays(LOOKBACK_DAYS);
        LocalDateTime until = LocalDateTime.now();
        
        // Fetch activity logs
        List<UserActivityLog> logs = activityLogRepository.findViewActivitiesInDateRange(since, until);
        
        if (logs.isEmpty()) {
            log.warn("No activity logs found in the last {} days", LOOKBACK_DAYS);
            return 0;
        }
        
        log.info("Processing {} activity logs from last {} days", logs.size(), LOOKBACK_DAYS);
        
        // Group by session ID to create transactions
        Map<String, Set<Integer>> sessionTransactions = new HashMap<>();
        for (UserActivityLog log : logs) {
            if (log.getCar() == null) continue;
            
            sessionTransactions
                    .computeIfAbsent(log.getSessionId(), k -> new HashSet<>())
                    .add(log.getCar().getCarId());
        }
        
        // Filter out single-item transactions (need at least 2 items)
        List<Set<Integer>> transactions = sessionTransactions.values().stream()
                .filter(set -> set.size() >= 2)
                .collect(Collectors.toList());
        
        log.info("Created {} transactions from {} sessions", 
                transactions.size(), sessionTransactions.size());
        
        if (transactions.isEmpty()) {
            log.warn("No multi-item transactions found");
            return 0;
        }
        
        // Run Apriori algorithm
        List<AprioriEngine.AssociationRule> rules = aprioriEngine.generateRules(
                transactions, MIN_SUPPORT, MIN_CONFIDENCE
        );
        
        if (rules.isEmpty()) {
            log.warn("No association rules generated");
            return 0;
        }
        
        // Clear old rules
        aprioriRuleRepository.deleteAll();
        log.info("Cleared old Apriori rules");
        
        // Save new rules
        int savedCount = 0;
        for (AprioriEngine.AssociationRule rule : rules) {
            try {
                Optional<Car> antecedentCar = carRepository.findById(rule.getAntecedent());
                Optional<Car> consequentCar = carRepository.findById(rule.getConsequent());
                
                if (antecedentCar.isPresent() && consequentCar.isPresent()) {
                    AprioriRule entity = AprioriRule.builder()
                            .antecedentCar(antecedentCar.get())
                            .consequentCar(consequentCar.get())
                            .confidence(rule.getConfidence())
                            .support(rule.getSupport())
                            .lift(rule.getLift())
                            .generatedAt(LocalDateTime.now())
                            .build();
                    
                    aprioriRuleRepository.save(entity);
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to save rule: {}", rule, e);
            }
        }
        
        log.info("Successfully saved {} Apriori rules", savedCount);
        return savedCount;
    }
    
    /**
     * Convert Car entity to CarSuggestionDto
     */
    private CarSuggestionDto convertToCarSuggestionDto(Car car) {
        if (car == null) return null;
        
        return CarSuggestionDto.builder()
                .carId(car.getCarId())
                .name(car.getName())
                .model(car.getModel())
                .thumbnail(car.getThumbnail())
                .price(car.getPrice())
                .build();
    }
}
