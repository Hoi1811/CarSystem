package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.UserActivityLogRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.UserActivityLogService;

import java.time.LocalDateTime;

/**
 * Implementation of UserActivityLogService with async logging
 * Logs user activities without blocking main request threads
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLogServiceImpl implements UserActivityLogService {
    
    private final UserActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    
    @Override
    @Async
    @Transactional
    public void logViewCar(Long userId, String sessionId, Integer carId, String ipAddress) {
        logActivity(userId, sessionId, ActivityType.VIEW_CAR, carId, null, null, ipAddress);
    }
    
    @Override
    @Async
    @Transactional
    public void logViewInventoryCar(Long userId, String sessionId, Long inventoryCarId, Integer carId, String ipAddress) {
        logActivity(userId, sessionId, ActivityType.VIEW_CAR, carId, inventoryCarId, null, ipAddress);
    }
    
    @Override
    @Async
    @Transactional
    public void logSearch(Long userId, String sessionId, String keyword, String ipAddress) {
        logActivity(userId, sessionId, ActivityType.SEARCH, null, null, keyword, ipAddress);
    }
    
    @Override
    @Async
    @Transactional
    public void logActivity(
            Long userId,
            String sessionId,
            ActivityType actionType,
            Integer carId,
            Long inventoryCarId,
            String searchKeyword,
            String ipAddress
    ) {
        try {
            // Build activity log
            UserActivityLog.UserActivityLogBuilder<?, ?> builder = UserActivityLog.builder()
                    .sessionId(sessionId)
                    .actionType(actionType)
                    .inventoryCarId(inventoryCarId)
                    .searchKeyword(searchKeyword)
                    .ipAddress(ipAddress)
                    .activityTimestamp(LocalDateTime.now());
            
            // Optional: Link to user if logged in
            if (userId != null) {
                userRepository.findById(userId).ifPresent(builder::user);
            }
            
            // Optional: Link to car if provided
            if (carId != null) {
                carRepository.findById(carId).ifPresent(builder::car);
            }
            
            UserActivityLog log = builder.build();
            activityLogRepository.save(log);
            
        } catch (Exception e) {
            // Don't throw exception - logging should not break main flow
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }
}
