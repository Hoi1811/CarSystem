package web.car_system.Car_Service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.analytics.OrderStatisticsDto;
import web.car_system.Car_Service.domain.dto.analytics.SalesStaffPerformanceDto;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.payment.AddPaymentRequest;
import web.car_system.Car_Service.domain.dto.payment.PaymentTransactionDto;
import web.car_system.Car_Service.domain.dto.sales_order.CreateOrderRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderFilterRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderStatusHistoryDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderSummaryDto;
import web.car_system.Car_Service.domain.dto.sales_order.UpdateOrderRequest;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.service.OrderAnalyticsService;
import web.car_system.Car_Service.service.PaymentTransactionService;
import web.car_system.Car_Service.service.SalesOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static web.car_system.Car_Service.constant.Endpoint.V1.SALES_ORDER.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequiredArgsConstructor
@Tag(name = "Sales Orders", description = "Sales order management APIs including order creation, payment processing, status tracking, and analytics")
@SecurityRequirement(name = "bearerAuth")
public class SalesOrderController {
    
    private final SalesOrderService salesOrderService;
    private final PaymentTransactionService paymentService;
    private final OrderAnalyticsService analyticsService;
    
    // ===== ADMIN ENDPOINTS =====
    
    /**
     * Create a new sales order
     * POST /api/v1/admin/sales-orders
     */
    @PostMapping(CREATE_ORDER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        OrderDto order = salesOrderService.createOrder(request);
        return success(order, "Tạo đơn hàng thành công: " + order.getOrderNumber(), HttpStatus.CREATED);
    }
    
    /**
     * Get all orders with pagination
     * GET /api/v1/admin/sales-orders?page=0&size=20&sort=orderDate,desc
     */
    @GetMapping(GET_ALL_ORDERS)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<OrderSummaryDto>>> getAllOrders(
            Pageable pageable) {
        
        Page<OrderSummaryDto> orders = salesOrderService.getAllOrders(pageable);
        return successPageable(orders, "Lấy danh sách đơn hàng thành công");
    }
    
    /**
     * Search/filter orders with pagination
     * GET /api/v1/admin/sales-orders/search?keyword=Nguyen&orderStatus=PAID&page=0&size=20
     */
    @GetMapping(ADMIN_PREFIX + "/search")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<OrderSummaryDto>>> searchOrders(
            OrderFilterRequest filter,
            Pageable pageable) {
        
        Page<OrderSummaryDto> orders = salesOrderService.searchOrders(filter, pageable);
        return successPageable(orders, "Tìm kiếm đơn hàng thành công");
    }
    
    /**
     * Get status change history for an order
     * GET /api/v1/admin/sales-orders/{id}/history
     */
    @GetMapping(ADMIN_PREFIX + "/{id}/history")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<OrderStatusHistoryDto>>> getOrderHistory(
            @PathVariable Long id) {
        
        List<OrderStatusHistoryDto> history = salesOrderService.getOrderStatusHistory(id);
        return success(history, "Lấy lịch sử thay đổi trạng thái thành công");
    }
    
    /**
     * Get order by ID
     * GET /api/v1/admin/sales-orders/{id}
     */
    @GetMapping(GET_ORDER_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> getOrderById(
            @PathVariable Long id) {
        
        OrderDto order = salesOrderService.getOrderById(id);
        return success(order, "Lấy thông tin đơn hàng thành công");
    }
    
    /**
     * Update an existing order
     * PUT /api/v1/admin/sales-orders/{id}
     */
    @PutMapping(UPDATE_ORDER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        
        OrderDto order = salesOrderService.updateOrder(id, request);
        return success(order, "Cập nhật đơn hàng thành công");
    }
    
    /**
     * Update order status
     * PATCH /api/v1/admin/sales-orders/{id}/status
     * Body: { "status": "PAID", "reason": "Optional reason" }
     */
    @PatchMapping(UPDATE_STATUS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        OrderStatus newStatus = OrderStatus.valueOf(request.get("status"));
        String reason = request.get("reason");
        
        OrderDto order = salesOrderService.updateOrderStatus(id, newStatus, reason);
        return success(order, "Cập nhật trạng thái đơn hàng thành công");
    }
    
    /**
     * Cancel an order
     * POST /api/v1/admin/sales-orders/{id}/cancel
     * Body: { "reason": "Cancellation reason" }
     */
    @PostMapping(CANCEL_ORDER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> cancelOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        OrderDto order = salesOrderService.cancelOrder(id, reason);
        return success(order, "Hủy đơn hàng thành công");
    }
    
    /**
     * Soft delete an order
     * DELETE /api/v1/admin/sales-orders/{id}
     */
    @DeleteMapping(DELETE_ORDER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteOrder(
            @PathVariable Long id) {
        
        salesOrderService.deleteOrder(id);
        return success(null, "Xóa đơn hàng thành công");
    }
    
    // ===== PAYMENT ENDPOINTS =====
    
    /**
     * Add payment to an order
     * POST /api/v1/admin/sales-orders/{orderId}/payments
     */
    @PostMapping(ADMIN_PREFIX + "/{orderId}/payments")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, PaymentTransactionDto>> addPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody AddPaymentRequest request) {
        
        PaymentTransactionDto payment = paymentService.addPayment(orderId, request);
        return success(payment, 
            String.format("Ghi nhận thanh toán %,.0f VNĐ thành công", 
                payment.getAmount().doubleValue()), 
            HttpStatus.CREATED);
    }
    
    /**
     * Get payment history for an order
     * GET /api/v1/admin/sales-orders/{orderId}/payments
     */
    @GetMapping(ADMIN_PREFIX + "/{orderId}/payments")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<PaymentTransactionDto>>> getPaymentHistory(
            @PathVariable Long orderId) {
        
        List<PaymentTransactionDto> payments = paymentService.getPaymentHistory(orderId);
        return success(payments, "Lấy lịch sử thanh toán thành công");
    }
    
    /**
     * Get payment details by ID
     * GET /api/v1/admin/payments/{id}
     */
    @GetMapping(Endpoint.V1.PREFIX + "/admin/payments/{id}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, PaymentTransactionDto>> getPaymentById(
            @PathVariable Long id) {
        
        PaymentTransactionDto payment = paymentService.getPaymentById(id);
        return success(payment, "Lấy thông tin thanh toán thành công");
    }
    
    // ===== ANALYTICS ENDPOINTS =====
    
    /**
     * Get order statistics
     * GET /api/v1/admin/sales-orders/analytics/statistics?fromDate=2024-01-01&toDate=2024-12-31
     */
    @GetMapping(ADMIN_PREFIX + "/analytics/statistics")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderStatisticsDto>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        OrderStatisticsDto stats = analyticsService.getOrderStatistics(fromDate, toDate);
        return success(stats, "Lấy thống kê đơn hàng thành công");
    }
    
    /**
     * Get top sales staff performance
     * GET /api/v1/admin/sales-orders/analytics/top-staff?limit=10&fromDate=2024-01-01
     */
    @GetMapping(ADMIN_PREFIX + "/analytics/top-staff")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<SalesStaffPerformanceDto>>> getTopSalesStaff(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<SalesStaffPerformanceDto> topStaff = analyticsService.getTopSalesStaff(limit, fromDate, toDate);
        return success(topStaff, "Lấy bảng xếp hạng nhân viên thành công");
    }
    
    // ===== PUBLIC ENDPOINTS =====
    
    /**
     * Track order by order number and phone (no auth required)
     * GET /api/v1/orders/track?orderNumber=ORD-2024-00001&phone=0901234567
     */
    @GetMapping(TRACK_ORDER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, OrderDto>> trackOrder(
            @RequestParam String orderNumber,
            @RequestParam String phone) {
        
        // Get order by number
        OrderDto order = salesOrderService.getOrderByNumber(orderNumber);
        
        // Verify phone matches
        if (!order.getCustomerPhone().equals(phone)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GlobalResponseDTO.<NoPaginatedMeta, OrderDto>builder()
                    .meta(NoPaginatedMeta.builder()
                        .message("Số điện thoại không khớp")
                        .build())
                    .build());
        }
        
        return success(order, "Tìm thấy đơn hàng");
    }
}
