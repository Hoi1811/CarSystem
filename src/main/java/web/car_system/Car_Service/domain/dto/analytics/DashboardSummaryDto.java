package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Main DTO that wraps all dashboard analytics data
 * Used for initial dashboard load to fetch all necessary data in one API call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    
    // Sales metrics
    private OrderStatisticsDto orderStatistics;
    private List<OrderStatusStatDto> orderStatusDistribution;
    private List<SalesStaffPerformanceDto> topSalesStaff;
    
    // Revenue trends
    private List<RevenueTrendDto> revenueMonthly;
    
    // User & system metrics
    private SystemHealthDto systemHealth;
    private List<UserGrowthDto> userGrowth;
}
