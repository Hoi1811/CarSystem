package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.sales_order.CreateOrderRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderFilterRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderStatusHistoryDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderSummaryDto;
import web.car_system.Car_Service.domain.dto.sales_order.UpdateOrderRequest;
import web.car_system.Car_Service.domain.entity.OrderStatus;

import java.util.List;

public interface SalesOrderService {
    
    /**
     * Create a new sales order
     * - Validates inventory car availability
     * - Calculates pricing
     * - Reserves the car (AVAILABLE → RESERVED)
     * 
     * @param request Order creation request
     * @return Created order details
     */
    OrderDto createOrder(CreateOrderRequest request);
    
    /**
     * Get all orders with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of order summaries
     */
    Page<OrderSummaryDto> getAllOrders(Pageable pageable);
    
    /**
     * Get order by ID with full details
     * 
     * @param id Order ID
     * @return Full order details
     */
    OrderDto getOrderById(Long id);
    
    /**
     * Get order by order number
     * 
     * @param orderNumber Order number (e.g., ORD-2024-00001)
     * @return Full order details
     */
    OrderDto getOrderByNumber(String orderNumber);
    
    /**
     * Update existing order
     * - Recalculates pricing if fees/discounts changed
     * - Validates status transitions
     * - Syncs inventory car status
     * 
     * @param id Order ID
     * @param request Update request
     * @return Updated order details
     */
    OrderDto updateOrder(Long id, UpdateOrderRequest request);
    
    /**
     * Update order status
     * - Validates status transition
     * - Syncs inventory car status
     * - Logs status change
     * 
     * @param id Order ID
     * @param newStatus New status
     * @param reason Reason for change (optional)
     * @return Updated order details
     */
    OrderDto updateOrderStatus(Long id, OrderStatus newStatus, String reason);
    
    /**
     * Cancel an order
     * - Sets status to CANCELLED
     * - Releases inventory car (RESERVED → AVAILABLE)
     * - Records cancellation reason
     * 
     * @param id Order ID
     * @param reason Cancellation reason
     * @return Updated order details
     */
    OrderDto cancelOrder(Long id, String reason);
    
    /**
     * Search/filter orders with pagination
     * - Dynamic query based on filter criteria
     * - Supports keyword search, status, date range, price, etc.
     * 
     * @param filter Filter criteria
     * @param pageable Pagination parameters
     * @return Page of filtered order summaries
     */
    Page<OrderSummaryDto> searchOrders(OrderFilterRequest filter, Pageable pageable);
    
    /**
     * Get status change history for an order
     * 
     * @param orderId Order ID
     * @return List of status changes ordered by time desc
     */
    List<OrderStatusHistoryDto> getOrderStatusHistory(Long orderId);
    
    /**
     * Soft delete an order
     * 
     * @param id Order ID
     */
    void deleteOrder(Long id);
}
