package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.entity.ActivityType;

/**
 * Service for logging user activities asynchronously
 * Used by Apriori recommendation system to track user behavior
 */
public interface UserActivityLogService {
    
    /**
     * Log when a user views a car model
     * @param userId User ID (can be null for guests)
     * @param sessionId HTTP session ID
     * @param carId Car model ID
     * @param ipAddress User's IP address (for spam prevention)
     */
    void logViewCar(Long userId, String sessionId, Integer carId, String ipAddress);
    
    /**
     * Log when a user views an inventory car details
     * @param userId User ID (can be null for guests)
     * @param sessionId HTTP session ID
     * @param inventoryCarId Inventory car ID
     * @param carId Car model ID (for association rules)
     * @param ipAddress User's IP address
     */
    void logViewInventoryCar(Long userId, String sessionId, Long inventoryCarId, Integer carId, String ipAddress);
    
    /**
     * Log search activity
     * @param userId User ID (can be null for guests)
     * @param sessionId HTTP session ID
     * @param keyword Search keyword
     * @param ipAddress User's IP address
     */
    void logSearch(Long userId, String sessionId, String keyword, String ipAddress);
    
    /**
     * Generic log activity method
     * @param userId User ID (nullable)
     * @param sessionId Session ID
     * @param actionType Type of action
     * @param carId Car ID (nullable)
     * @param inventoryCarId Inventory car ID (nullable)
     * @param searchKeyword Search keyword (nullable)
     * @param ipAddress IP address
     */
    void logActivity(
            Long userId,
            String sessionId,
            ActivityType actionType,
            Integer carId,
            Long inventoryCarId,
            String searchKeyword,
            String ipAddress
    );
}
