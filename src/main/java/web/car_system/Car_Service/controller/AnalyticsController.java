package web.car_system.Car_Service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.analytics.DashboardSummaryDto;
import web.car_system.Car_Service.domain.dto.analytics.OrderStatusStatDto;
import web.car_system.Car_Service.domain.dto.analytics.RevenueTrendDto;
import web.car_system.Car_Service.domain.dto.analytics.SystemHealthDto;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.service.DashboardAnalyticsService;

import java.time.LocalDate;
import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.ANALYTICS.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;

/**
 * REST Controller for Dashboard Analytics
 * Provides optimized endpoints for business intelligence and reporting
 */
@RestApiV1
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard analytics and business intelligence APIs")
public class AnalyticsController {
    
    private final DashboardAnalyticsService dashboardAnalyticsService;
    
    @GetMapping(DASHBOARD)
    @Operation(
        summary = "Get complete dashboard summary",
        description = "Fetches all analytics data for the dashboard in a single optimized call. " +
                     "Includes order statistics, revenue trends, user metrics, and system health."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, DashboardSummaryDto>> getDashboardSummary(
        @Parameter(description = "Year for revenue trend data", example = "2025")
        @RequestParam(required = false) Integer year
    ) {
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        DashboardSummaryDto summary = dashboardAnalyticsService.getDashboardSummary(effectiveYear);
        return success(summary, "Lấy dữ liệu dashboard thành công");
    }
    
    @GetMapping(MONTHLY_REVENUE)
    @Operation(
        summary = "Get monthly revenue trends",
        description = "Returns revenue aggregated by month for the specified year"
    )
    @ApiResponse(responseCode = "200", description = "Revenue data retrieved successfully")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<RevenueTrendDto>>> getMonthlyRevenue(
        @Parameter(description = "Year to fetch revenue for", example = "2025")
        @RequestParam(required = false) Integer year
    ) {
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        List<RevenueTrendDto> trends = dashboardAnalyticsService.getMonthlyRevenue(effectiveYear);
        return success(trends, "Lấy dữ liệu doanh thu theo tháng thành công");
    }
    
    @GetMapping(STATUS_DISTRIBUTION)
    @Operation(
        summary = "Get order status distribution",
        description = "Returns count of orders grouped by status (for pie/doughnut charts)"
    )
    @ApiResponse(responseCode = "200", description = "Status distribution retrieved successfully")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<OrderStatusStatDto>>> getOrderStatusDistribution() {
        List<OrderStatusStatDto> distribution = dashboardAnalyticsService.getOrderStatusDistribution();
        return success(distribution, "Lấy phân bổ trạng thái đơn hàng thành công");
    }
    
    @GetMapping(SYSTEM_HEALTH)
    @Operation(
        summary = "Get system health metrics",
        description = "Returns user statistics, lead metrics, and system status"
    )
    @ApiResponse(responseCode = "200", description = "System health data retrieved successfully")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, SystemHealthDto>> getSystemHealth() {
        SystemHealthDto health = dashboardAnalyticsService.getSystemHealth();
        return success(health, "Lấy thông tin sức khỏe hệ thống thành công");
    }
}
