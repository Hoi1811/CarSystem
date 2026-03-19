package web.car_system.Car_Service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import web.car_system.Car_Service.service.AprioriRecommendationService;

/**
 * Scheduled job to generate Apriori association rules
 * Runs nightly at 3:00 AM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AprioriRuleGenerationScheduler {
    
    private final AprioriRecommendationService recommendationService;
    
    /**
     * Generate Apriori rules every day at 3:00 AM
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 3 * * *")  // 3:00 AM every day
    public void generateAprioriRules() {
        log.info("========== Apriori Rule Generation Job Started ==========");
        
        try {
            int rulesGenerated = recommendationService.generateAprioriRules();
            log.info("Successfully generated {} Apriori rules", rulesGenerated);
        } catch (Exception e) {
            log.error("Failed to generate Apriori rules", e);
        }
        
        log.info("========== Apriori Rule Generation Job Completed ==========");
    }
    
    /**
     * Optional: Run every 6 hours for testing/development
     * Uncomment for more frequent updates
     */
    // @Scheduled(cron = "0 0 */6 * * *")  // Every 6 hours
    // public void generateAprioriRulesFrequent() {
    //     generateAprioriRules();
    // }
}
