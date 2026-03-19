package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entity to track user activity for recommendation system
 * Uses Apriori algorithm to find association rules between car views
 */
@Entity
@Table(name = "user_activity_logs", indexes = {
        @Index(name = "idx_user_session", columnList = "user_id,session_id"),
        @Index(name = "idx_session_created", columnList = "session_id,created_at"),
        @Index(name = "idx_car_action", columnList = "car_id,action_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE user_activity_logs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserActivityLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who performed the action (nullable for guest users)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Session ID for grouping activities (important for Apriori algorithm)
     * Format: UUID or sessionId from HttpSession
     */
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;
    
    /**
     * Type of activity performed
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActivityType actionType;
    
    /**
     * Car model being viewed/interacted with
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;
    
    /**
     * Specific inventory car if viewing inventory details
     */
    @Column(name = "inventory_car_id")
    private Long inventoryCarId;
    
    /**
     * Search keyword if action is SEARCH
     */
    @Column(name = "search_keyword")
    private String searchKeyword;
    
    /**
     * IP address for spam prevention
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * Timestamp of the activity
     */
    @Column(name = "activity_timestamp", nullable = false)
    private LocalDateTime activityTimestamp;
}
