package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.ActivityType;
import web.car_system.Car_Service.domain.entity.UserActivityLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    /**
     * Find all activities for a specific session (for Apriori algorithm)
     */
    List<UserActivityLog> findBySessionIdOrderByActivityTimestampAsc(String sessionId);
    
    /**
     * Find recent activities by user (for personalized recommendations)
     */
    @Query("SELECT l FROM UserActivityLog l " +
           "WHERE l.user.userId = :userId " +
           "AND l.actionType = :actionType " +
           "AND l.activityTimestamp >= :since " +
           "ORDER BY l.activityTimestamp DESC")
    List<UserActivityLog> findRecentUserActivities(
            @Param("userId") Long userId,
            @Param("actionType") ActivityType actionType,
            @Param("since") LocalDateTime since
    );
    
    /**
     * Get all VIEW_CAR activities within a date range (for Apriori training)
     */
    @Query("SELECT l FROM UserActivityLog l " +
           "WHERE l.actionType = 'VIEW_CAR' " +
           "AND l.activityTimestamp BETWEEN :startDate AND :endDate " +
           "AND l.car IS NOT NULL " +
           "ORDER BY l.sessionId, l.activityTimestamp")
    List<UserActivityLog> findViewActivitiesInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find all sessions with their car views (grouped data for Apriori)
     */
    @Query("SELECT l.sessionId, l.car.carId " +
           "FROM UserActivityLog l " +
           "WHERE l.actionType = 'VIEW_CAR' " +
           "AND l.activityTimestamp >= :since " +
           "AND l.car IS NOT NULL " +
           "ORDER BY l.sessionId, l.activityTimestamp")
    List<Object[]> findSessionCarPairs(@Param("since") LocalDateTime since);
}
