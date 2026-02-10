package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.analytics.*;
import web.car_system.Car_Service.domain.entity.LeadStatus;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.repository.LeadRepository;
import web.car_system.Car_Service.repository.SalesOrderRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.DashboardAnalyticsService;
import web.car_system.Car_Service.service.OrderAnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Optimized Dashboard Analytics Service using DB Aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {
    
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final OrderAnalyticsService orderAnalyticsService; // Reuse existing service if needed
    
    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(int year) {
        log.info("Fetching dashboard summary for year {}", year);
        
        return DashboardSummaryDto.builder()
            .orderStatistics(getOrderStatisticsOptimized())
            .orderStatusDistribution(getOrderStatusDistribution())
            .topSalesStaff(getTopSalesStaff(5))
            .revenueMonthly(getMonthlyRevenue(year))
            .systemHealth(getSystemHealth())
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendDto> getMonthlyRevenue(int year) {
        log.info("Fetching monthly revenue for year {}", year);
        
        List<Object[]> results = salesOrderRepository.findMonthlyRevenueByYear(year);
        List<RevenueTrendDto> trends = new ArrayList<>();
        
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            trends.add(RevenueTrendDto.builder()
                .month(month)
                .year(year)
                .amount(amount)
                .build());
        }
        
        return trends;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusStatDto> getOrderStatusDistribution() {
        log.info("Fetching order status distribution");
        
        List<Object[]> results = salesOrderRepository.countOrdersByStatus();
        List<OrderStatusStatDto> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            
            stats.add(OrderStatusStatDto.builder()
                .status(status)
                .count(count)
                .statusLabel(status.name())
                .build());
        }
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public SystemHealthDto getSystemHealth() {
        log.info("Fetching system health metrics");
        
        // User metrics
        long totalUsers = userRepository.countTotalUsers();
        long totalCustomers = userRepository.countByRoleName("ROLE_CUSTOMER");
        
        // Calculate "this month" start date
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        long newUsersThisMonth = userRepository.countNewUsersSince(startOfMonth);
        
        // Lead metrics
        long totalLeads = leadRepository.countTotalLeads();
        long newLeadsThisMonth = leadRepository.countNewLeadsSince(startOfMonth);
        long activeLeads = leadRepository.countByLeadStatus(LeadStatus.NEW) + 
                          leadRepository.countByLeadStatus(LeadStatus.PROCESSING);
        
        return SystemHealthDto.builder()
            .totalUsers(totalUsers)
            .totalCustomers(totalCustomers)
            .newUsersThisMonth(newUsersThisMonth)
            .activeUsersLast24h(0L) // TODO: Implement if tracking last_login
            .totalLeads(totalLeads)
            .newLeadsThisMonth(newLeadsThisMonth)
            .activeLeads(activeLeads)
            .serverStatus("HEALTHY")
            .uptime(0L) // TODO: Implement actual uptime tracking
            .build();
    }
    
    /**
     * Optimized Order Statistics using direct DB queries
     */
    private OrderStatisticsDto getOrderStatisticsOptimized() {
        log.info("Calculating optimized order statistics");
        
        // Get status distribution
        List<OrderStatusStatDto> statusStats = getOrderStatusDistribution();
        
        // Convert to map
        java.util.Map<String, Long> ordersByStatus = new java.util.HashMap<>();
        long totalOrders = 0;
        long completedOrders = 0;
        long cancelledOrders = 0;
        
        for (OrderStatusStatDto stat : statusStats) {
            ordersByStatus.put(stat.getStatus().name(), stat.getCount());
            totalOrders += stat.getCount();
            
            if (stat.getStatus() == OrderStatus.COMPLETED) {
                completedOrders = stat.getCount();
            } else if (stat.getStatus() == OrderStatus.CANCELLED) {
                cancelledOrders = stat.getCount();
            }
        }
        
        long activeOrders = totalOrders - completedOrders - cancelledOrders;
        
        // Get revenue metrics
        BigDecimal totalRevenue = salesOrderRepository.getTotalRevenue();
        BigDecimal totalPaid = salesOrderRepository.getTotalPaidAmount();
        
        // Calculate averages
        BigDecimal averageOrderValue = completedOrders > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return OrderStatisticsDto.builder()
            .ordersByStatus(ordersByStatus)
            .totalRevenue(totalRevenue)
            .totalPaid(totalPaid)
            .totalPending(BigDecimal.ZERO) // Can calculate if needed
            .totalOrders(totalOrders)
            .completedOrders(completedOrders)
            .cancelledOrders(cancelledOrders)
            .activeOrders(activeOrders)
            .averageOrderValue(averageOrderValue)
            .build();
    }
    
    /**
     * Get top sales staff using optimized query
     */
    private List<SalesStaffPerformanceDto> getTopSalesStaff(int limit) {
        log.info("Fetching top {} sales staff", limit);
        
        List<Object[]> results = salesOrderRepository.findTopSalesStaff(PageRequest.of(0, limit));
        List<SalesStaffPerformanceDto> staff = new ArrayList<>();
        
        for (Object[] row : results) {
            Long staffId = (Long) row[0];
            String staffName = (String) row[1];
            Long totalOrders = (Long) row[2];
            BigDecimal totalRevenue = (BigDecimal) row[3];
            
            staff.add(SalesStaffPerformanceDto.builder()
                .staffId(staffId)
                .staffName(staffName)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .completedOrders(0L) // Can add if needed
                .conversionRate(0.0) // Can calculate if needed
                .build());
        }
        
        return staff;
    }
}
