package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.analytics.DashboardSummaryDto;
import web.car_system.Car_Service.domain.dto.analytics.OrderStatusStatDto;
import web.car_system.Car_Service.domain.dto.analytics.RevenueTrendDto;
import web.car_system.Car_Service.domain.dto.analytics.SystemHealthDto;

import java.util.List;

/**
 * Service for dashboard analytics and business intelligence
 */
public interface DashboardAnalyticsService {
    
    /**
     * Get complete dashboard summary with all metrics
     * Optimized single call for dashboard initialization
     */
    DashboardSummaryDto getDashboardSummary(int year);
    
    /**
     * Get monthly revenue trend for specific year
     */
    List<RevenueTrendDto> getMonthlyRevenue(int year);
    
    /**
     * Get order distribution by status
     */
    List<OrderStatusStatDto> getOrderStatusDistribution();
    
    /**
     * Get system health metrics (users, leads, traffic)
     */
    SystemHealthDto getSystemHealth();
}
