package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.sales_order.CreateOrderRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderFilterRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderStatusHistoryDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderSummaryDto;
import web.car_system.Car_Service.domain.dto.sales_order.UpdateOrderRequest;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.OrderStatusHistoryMapper;
import web.car_system.Car_Service.domain.mapper.SalesOrderMapper;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.LeadRepository;
import web.car_system.Car_Service.repository.OrderStatusHistoryRepository;
import web.car_system.Car_Service.repository.SalesOrderRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.SalesOrderService;
import web.car_system.Car_Service.specification.SalesOrderSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryCarRepository inventoryCarRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderStatusHistoryMapper statusHistoryMapper;
    
    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerName());
        
        // 1. Validate and fetch inventory car
        InventoryCar car = inventoryCarRepository.findById(request.getInventoryCarId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy xe trong kho với ID: " + request.getInventoryCarId()));
        
        // 2. Check if car is available
        if (car.getSaleStatus() != SaleStatus.AVAILABLE) {
            throw new BusinessException(
                "Xe này hiện " + getSaleStatusMessage(car.getSaleStatus()) + 
                ". Vui lòng chọn xe khác.");
        }
        
        // 3. Check for existing active orders on this car
        if (salesOrderRepository.findActiveOrderByInventoryCarId(car.getId()).isPresent()) {
            throw new BusinessException(
                "Xe này đã có đơn hàng đang xử lý. Vui lòng chọn xe khác.");
        }
        
        // 4. Map to entity
        SalesOrder order = salesOrderMapper.toEntity(request);
        order.setInventoryCar(car);
        
        // 5. Set relationships if provided
        if (request.getSalesStaffId() != null) {
            User salesStaff = userRepository.findById(request.getSalesStaffId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Không tìm thấy nhân viên với ID: " + request.getSalesStaffId()));
            order.setSalesStaff(salesStaff);
        }
        
        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Không tìm thấy lead với ID: " + request.getLeadId()));
            order.setLead(lead);
        }
        
        // 6. Calculate pricing
        order.calculateTotalPrice();
        order.calculateRemainingAmount();
        
        // 7. Validate deposit amount
        if (request.getDepositAmount() != null && request.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            validateDeposit(request.getDepositAmount(), order.getTotalPrice());
        }
        
        // 8. Reserve the car
        car.setSaleStatus(SaleStatus.RESERVED);
        inventoryCarRepository.save(car);
        
        // 9. Save order
        SalesOrder savedOrder = salesOrderRepository.save(order);
        
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return salesOrderMapper.toDto(savedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getAllOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable)
            .map(salesOrderMapper::toSummaryDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + id));
        return salesOrderMapper.toDto(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        SalesOrder order = salesOrderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng: " + orderNumber));
        return salesOrderMapper.toDto(order);
    }
    
    @Override
    @Transactional
    public OrderDto updateOrder(Long id, UpdateOrderRequest request) {
        log.info("Updating order ID: {}", id);
        
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + id));
        
        // Check if order can be updated
        if (order.getOrderStatus() == OrderStatus.COMPLETED || 
            order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Không thể cập nhật đơn hàng đã hoàn tất hoặc đã hủy");
        }
        
        // Update sales staff if changed
        if (request.getSalesStaffId() != null) {
            User salesStaff = userRepository.findById(request.getSalesStaffId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Không tìm thấy nhân viên với ID: " + request.getSalesStaffId()));
            order.setSalesStaff(salesStaff);
        }
        
        // Update fields from request
        salesOrderMapper.updateFromDto(request, order);
        
        // Recalculate if fees/discounts changed
        if (request.getAdditionalFees() != null || request.getDiscountAmount() != null) {
            order.calculateTotalPrice();
            order.calculateRemainingAmount();
        }
        
        // Handle status change if provided
        if (request.getOrderStatus() != null && 
            !order.getOrderStatus().equals(request.getOrderStatus())) {
            updateOrderStatusInternal(order, request.getOrderStatus(), null);
        }
        
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        log.info("Order updated successfully: {}", updatedOrder.getOrderNumber());
        
        return salesOrderMapper.toDto(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus newStatus, String reason) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + id));
        
        updateOrderStatusInternal(order, newStatus, reason);
        
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDto(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderDto cancelOrder(Long id, String reason) {
        log.info("Cancelling order ID: {}", id);
        
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + id));
        
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Không thể hủy đơn hàng đã hoàn tất");
        }
        
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Đơn hàng đã bị hủy trước đó");
        }
        
        // Log status change before updating
        OrderStatus oldStatus = order.getOrderStatus();
        
        // Update order
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        order.setCancelledDate(LocalDate.now());
        
        // Record in status history
        logStatusChange(order, oldStatus, OrderStatus.CANCELLED, reason);
        
        // Release inventory car
        InventoryCar car = order.getInventoryCar();
        car.setSaleStatus(SaleStatus.AVAILABLE);
        inventoryCarRepository.save(car);
        
        SalesOrder cancelledOrder = salesOrderRepository.save(order);
        log.info("Order cancelled: {} - Reason: {}", cancelledOrder.getOrderNumber(), reason);
        
        return salesOrderMapper.toDto(cancelledOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> searchOrders(OrderFilterRequest filter, Pageable pageable) {
        log.info("Searching orders with filter: {}", filter);
        
        // Build specification from filter
        Specification<SalesOrder> spec = SalesOrderSpecification.filterOrders(filter);
        
        // Execute query with pagination
        return salesOrderRepository.findAll(spec, pageable)
            .map(salesOrderMapper::toSummaryDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDto> getOrderStatusHistory(Long orderId) {
        // Verify order exists
        if (!salesOrderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
        }
        
        return statusHistoryRepository.findBySalesOrderIdOrderByChangedAtDesc(orderId)
            .stream()
            .map(statusHistoryMapper::toDto)
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + id));
        
        // Only allow deletion of draft or cancelled orders
        if (order.getOrderStatus() != OrderStatus.DRAFT && 
            order.getOrderStatus() != OrderStatus.CANCELLED) {
            throw new BusinessException(
                "Chỉ có thể xóa đơn hàng ở trạng thái DRAFT hoặc CANCELLED");
        }
        
        // Release car if reserved
        if (order.getInventoryCar().getSaleStatus() == SaleStatus.RESERVED) {
            InventoryCar car = order.getInventoryCar();
            car.setSaleStatus(SaleStatus.AVAILABLE);
            inventoryCarRepository.save(car);
        }
        
        salesOrderRepository.delete(order);
        log.info("Order deleted: {}", order.getOrderNumber());
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Internal method to update order status and sync inventory
     */
    private void updateOrderStatusInternal(SalesOrder order, OrderStatus newStatus, String reason) {
        OrderStatus oldStatus = order.getOrderStatus();
        
        // Validate status transition
        validateStatusTransition(order, newStatus);  // ✅ Fixed: pass order instead of oldStatus
        
        // Update status
        order.setOrderStatus(newStatus);
        
        // Record status change in history
        logStatusChange(order, oldStatus, newStatus, reason);
        
        // Sync inventory car status
        syncInventoryCarStatus(order, newStatus);
        
        // Save order
        salesOrderRepository.save(order);
        
        // Set completion/cancellation dates
        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedDate(LocalDate.now());
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledDate(LocalDate.now());
            if (reason != null) {
                order.setCancellationReason(reason);
            }
        } else if (newStatus == OrderStatus.DELIVERED && order.getActualDeliveryDate() == null) {
            order.setActualDeliveryDate(LocalDate.now());
        }
        
        log.info("Order {} status changed: {} → {}", 
            order.getOrderNumber(), oldStatus, newStatus);
    }
    
    /**
     * Log status change to history
     */
    private void logStatusChange(SalesOrder order, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
            .salesOrder(order)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .changeReason(reason)
            .build();
        
        // Try to get current user
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                userRepository.findByUsername(username).ifPresent(history::setChangedBy);
            }
        } catch (Exception e) {
            log.warn("Could not get current user for status change: {}", e.getMessage());
        }
        
        statusHistoryRepository.save(history);
        log.debug("Status change logged for order {}: {} → {}", 
            order.getOrderNumber(), oldStatus, newStatus);
    }
    
    /**
     * Validate status transition rules
     */
    private void validateStatusTransition(SalesOrder order, OrderStatus target) {
        OrderStatus current = order.getOrderStatus();
        
        // Define allowed transitions
        List<OrderStatus> allowedNextStatuses = switch (current) {
            case DRAFT -> List.of(OrderStatus.PENDING_DEPOSIT, OrderStatus.CANCELLED);
            case PENDING_DEPOSIT -> List.of(OrderStatus.DEPOSIT_PAID, OrderStatus.CANCELLED);
            case DEPOSIT_PAID -> List.of(
                OrderStatus.PENDING_FINAL_PAYMENT, 
                OrderStatus.CANCELLED,              // ✅ FIX: Allow cancel after deposit
                OrderStatus.REFUND_PROCESSING
            );
            case PENDING_FINAL_PAYMENT -> List.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> List.of(OrderStatus.PREPARING_DELIVERY);
            case PREPARING_DELIVERY -> List.of(OrderStatus.DELIVERED);
            case DELIVERED -> List.of(OrderStatus.COMPLETED);
            case REFUND_PROCESSING -> List.of(OrderStatus.CANCELLED);
            case COMPLETED, CANCELLED -> List.of(); // Terminal states
        };
        
        if (!allowedNextStatuses.contains(target)) {
            throw new BusinessException(
                String.format("Không thể chuyển từ trạng thái %s sang %s. Các trạng thái hợp lệ: %s", 
                    current, target, allowedNextStatuses));
        }
        
        // ========================================
        // ✅ ENHANCEMENT: Additional business rule validation
        // ========================================
        
        // Cannot deliver unpaid orders
        if (target == OrderStatus.DELIVERED) {
            if (order.getPaymentStatus() != PaymentStatus.FULLY_PAID) {
                throw new BusinessException(
                    "Không thể giao xe khi chưa thanh toán đủ. Còn thiếu: " + 
                    formatVND(order.getRemainingAmount())
                );
            }
        }
        
        // Cannot transition to PAID if not fully paid
        if (target == OrderStatus.PAID) {
            if (order.getPaymentStatus() != PaymentStatus.FULLY_PAID) {
                throw new BusinessException(
                    "Chưa thanh toán đủ để chuyển sang trạng thái PAID. Còn thiếu: " +
                    formatVND(order.getRemainingAmount())
                );
            }
        }
    }
    
    /**
     * Format VND currency
     */
    private String formatVND(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount.doubleValue());
    }
    
    /**
     * Sync inventory car status based on order status
     */
    private void syncInventoryCarStatus(SalesOrder order, OrderStatus newStatus) {
        InventoryCar car = order.getInventoryCar();
        
        switch (newStatus) {
            case DEPOSIT_PAID, PENDING_FINAL_PAYMENT, PAID, PREPARING_DELIVERY -> {
                if (car.getSaleStatus() != SaleStatus.RESERVED) {
                    car.setSaleStatus(SaleStatus.RESERVED);
                    inventoryCarRepository.save(car);
                }
            }
            case DELIVERED, COMPLETED -> {
                car.setSaleStatus(SaleStatus.SOLD);
                inventoryCarRepository.save(car);
            }
            case CANCELLED -> {
                car.setSaleStatus(SaleStatus.AVAILABLE);
                inventoryCarRepository.save(car);
            }
        }
    }
    
    /**
     * Validate deposit amount (10% - 50% of total)
     */
    private void validateDeposit(BigDecimal deposit, BigDecimal totalPrice) {
        BigDecimal minDeposit = totalPrice.multiply(BigDecimal.valueOf(0.10));
        BigDecimal maxDeposit = totalPrice.multiply(BigDecimal.valueOf(0.50));
        
        if (deposit.compareTo(minDeposit) < 0) {
            throw new BusinessException(
                String.format("Tiền cọc tối thiểu là %,.0f VNĐ (10%% tổng giá)", 
                    minDeposit.doubleValue()));
        }
        
        if (deposit.compareTo(maxDeposit) > 0) {
            throw new BusinessException(
                String.format("Tiền cọc tối đa là %,.0f VNĐ (50%% tổng giá)", 
                    maxDeposit.doubleValue()));
        }
    }
    
    /**
     * Get user-friendly sale status message
     */
    private String getSaleStatusMessage(SaleStatus status) {
        return switch (status) {
            case AVAILABLE -> "đang sẵn sàng";
            case RESERVED -> "đã được đặt trước";
            case SOLD -> "đã được bán";
        };
    }
}
