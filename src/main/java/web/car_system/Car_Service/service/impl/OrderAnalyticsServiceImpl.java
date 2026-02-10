package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.analytics.OrderStatisticsDto;
import web.car_system.Car_Service.domain.dto.analytics.SalesStaffPerformanceDto;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.SalesOrder;
import web.car_system.Car_Service.repository.SalesOrderRepository;
import web.car_system.Car_Service.service.OrderAnalyticsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAnalyticsServiceImpl implements OrderAnalyticsService {
    
    private final SalesOrderRepository salesOrderRepository;
    
    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsDto getOrderStatistics(LocalDate fromDate, LocalDate toDate) {
        log.info("Calculating order statistics from {} to {}", fromDate, toDate);
        
        // Get all orders in date range
        List<SalesOrder> orders = salesOrderRepository.findAll().stream()
            .filter(order -> isInDateRange(order.getOrderDate(), fromDate, toDate))
            .collect(Collectors.toList());
        
        // Calculate metrics
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream()
                .filter(o -> o.getOrderStatus() == status)
                .count();
            if (count > 0) {
                ordersByStatus.put(status.name(), count);
            }
        }
        
        BigDecimal totalRevenue = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
            .map(SalesOrder::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = orders.stream()
            .map(SalesOrder::getPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPending = orders.stream()
            .filter(o -> o.getOrderStatus() != OrderStatus.COMPLETED && 
                        o.getOrderStatus() != OrderStatus.CANCELLED)
            .map(SalesOrder::getRemainingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalOrders = orders.size();
        long completedOrders = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
            .count();
        long cancelledOrders = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED)
            .count();
        long activeOrders = totalOrders - completedOrders - cancelledOrders;
        
        BigDecimal averageOrderValue = totalOrders > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(completedOrders > 0 ? completedOrders : 1), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return OrderStatisticsDto.builder()
            .ordersByStatus(ordersByStatus)
            .totalRevenue(totalRevenue)
            .totalPaid(totalPaid)
            .totalPending(totalPending)
            .totalOrders(totalOrders)
            .completedOrders(completedOrders)
            .cancelledOrders(cancelledOrders)
            .activeOrders(activeOrders)
            .averageOrderValue(averageOrderValue)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesStaffPerformanceDto> getTopSalesStaff(int limit, LocalDate fromDate, LocalDate toDate) {
        log.info("Calculating top {} sales staff performance", limit);
        
        // Get all orders in date range
        List<SalesOrder> orders = salesOrderRepository.findAll().stream()
            .filter(order -> order.getSalesStaff() != null)
            .filter(order -> isInDateRange(order.getOrderDate(), fromDate, toDate))
            .collect(Collectors.toList());
        
        // Group by staff
        Map<Long, List<SalesOrder>> ordersByStaff = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getSalesStaff().getUserId()));
        
        // Calculate performance for each staff
        return ordersByStaff.entrySet().stream()
            .map(entry -> {
                Long staffId = entry.getKey();
                List<SalesOrder> staffOrders = entry.getValue();
                
                long totalOrders = staffOrders.size();
                long completedOrders = staffOrders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                    .count();
                
                BigDecimal totalRevenue = staffOrders.stream()
                    .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                    .map(SalesOrder::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                double conversionRate = totalOrders > 0 
                    ? (double) completedOrders / totalOrders * 100
                    : 0.0;
                
                String staffName = staffOrders.get(0).getSalesStaff().getFullName();
                
                return SalesStaffPerformanceDto.builder()
                    .staffId(staffId)
                    .staffName(staffName)
                    .totalOrders(totalOrders)
                    .completedOrders(completedOrders)
                    .totalRevenue(totalRevenue)
                    .conversionRate(conversionRate)
                    .build();
            })
            .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private boolean isInDateRange(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        if (date == null) return false;
        if (fromDate != null && date.isBefore(fromDate)) return false;
        if (toDate != null && date.isAfter(toDate)) return false;
        return true;
    }
}
