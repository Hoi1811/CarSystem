package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for system health and traffic metrics
 * Used for dashboard summary cards
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDto {
    
    // User metrics
    private Long totalUsers;
    private Long totalCustomers;
    private Long newUsersThisMonth;
    private Long activeUsersLast24h;
    
    // Lead metrics
    private Long totalLeads;
    private Long newLeadsThisMonth;
    private Long activeLeads;  // Status = NEW or CONTACTED
    
    // System status
    private String serverStatus;  // "HEALTHY", "WARNING", "ERROR"
    private Long uptime;  // In seconds
}
