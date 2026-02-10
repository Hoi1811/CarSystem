package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.analytics.OrderStatisticsDto;
import web.car_system.Car_Service.domain.dto.analytics.SalesStaffPerformanceDto;

import java.time.LocalDate;
import java.util.List;

public interface OrderAnalyticsService {
    
    /**
     * Get overall order statistics
     * 
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     * @return Order statistics
     */
    OrderStatisticsDto getOrderStatistics(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Get top performing sales staff
     * 
     * @param limit Number of results
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     * @return List of staff performance
     */
    List<SalesStaffPerformanceDto> getTopSalesStaff(int limit, LocalDate fromDate, LocalDate toDate);
}
