package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.payment.AddPaymentRequest;
import web.car_system.Car_Service.domain.dto.payment.PaymentTransactionDto;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.PaymentTransactionMapper;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.repository.PaymentTransactionRepository;
import web.car_system.Car_Service.repository.SalesOrderRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.PaymentTransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionServiceImpl implements PaymentTransactionService {
    
    private final PaymentTransactionRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final PaymentTransactionMapper paymentMapper;
    
    @Override
    @Transactional
    public PaymentTransactionDto addPayment(Long orderId, AddPaymentRequest request) {
        log.info("Adding payment of {} VND to order ID: {}", request.getAmount(), orderId);
        
        // 1. Get order
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đơn hàng với ID: " + orderId));
        
        // 2. Validate order state
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Không thể thêm thanh toán cho đơn hàng đã hoàn tất");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Không thể thêm thanh toán cho đơn hàng đã hủy");
        }
        
        // 3. Validate payment amount
        validatePaymentAmount(order, request);
        
        // 4. Create payment transaction
        PaymentTransaction payment = PaymentTransaction.builder()
            .salesOrder(order)
            .amount(request.getAmount())
            .paymentType(request.getPaymentType())
            .paymentMethod(request.getPaymentMethod())
            .transactionDate(request.getTransactionDate())
            .transactionReference(request.getTransactionReference())
            .notes(request.getNotes())
            .build();
        
        // 5. Set received by (current user)
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                userRepository.findByUsername(username).ifPresent(payment::setReceivedBy);
            }
        } catch (Exception e) {
            log.warn("Could not get current user for payment: {}", e.getMessage());
        }
        
        // 6. Save payment
        PaymentTransaction savedPayment = paymentRepository.save(payment);
        
        // 7. Update order amounts and status
        updateOrderAfterPayment(order);
        
        log.info("Payment added successfully. Order {} - Amount: {} VND", 
            order.getOrderNumber(), request.getAmount());
        
        return paymentMapper.toDto(savedPayment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getPaymentHistory(Long orderId) {
        // Verify order exists
        if (!salesOrderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
        }
        
        return paymentRepository.findBySalesOrderIdOrderByTransactionDateDesc(orderId)
            .stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentTransactionDto getPaymentById(Long id) {
        PaymentTransaction payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy giao dịch thanh toán với ID: " + id));
        return paymentMapper.toDto(payment);
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Validate payment amount based on type and order state
     */
    private void validatePaymentAmount(SalesOrder order, AddPaymentRequest request) {
        BigDecimal amount = request.getAmount();
        BigDecimal remaining = order.getRemainingAmount();
        
        switch (request.getPaymentType()) {
            case DEPOSIT -> {
                // Validate deposit (10-50% of total)
                BigDecimal minDeposit = order.getTotalPrice().multiply(BigDecimal.valueOf(0.10));
                BigDecimal maxDeposit = order.getTotalPrice().multiply(BigDecimal.valueOf(0.50));
                
                if (amount.compareTo(minDeposit) < 0) {
                    throw new BusinessException(
                        String.format("Tiền cọc tối thiểu là %,.0f VNĐ (10%% tổng giá)", 
                            minDeposit.doubleValue()));
                }
                
                if (amount.compareTo(maxDeposit) > 0) {
                    throw new BusinessException(
                        String.format("Tiền cọc tối đa là %,.0f VNĐ (50%% tổng giá)", 
                            maxDeposit.doubleValue()));
                }
                
                // Check if already has deposit
                if (order.getPaymentStatus() != PaymentStatus.UNPAID) {
                    throw new BusinessException("Đơn hàng đã có tiền cọc rồi");
                }
            }
            
            case FINAL_PAYMENT -> {
                // Cannot exceed remaining amount
                if (amount.compareTo(remaining) > 0) {
                    throw new BusinessException(
                        String.format("Số tiền thanh toán (%,.0f VNĐ) vượt quá số tiền còn lại (%,.0f VNĐ)",
                            amount.doubleValue(), remaining.doubleValue()));
                }
            }
            
            case REFUND -> {
                // Refund cannot exceed paid amount
                BigDecimal totalPaid = order.getPaidAmount();
                if (amount.compareTo(totalPaid) > 0) {
                    throw new BusinessException(
                        String.format("Số tiền hoàn lại (%,.0f VNĐ) vượt quá số tiền đã thanh toán (%,.0f VNĐ)",
                            amount.doubleValue(), totalPaid.doubleValue()));
                }
            }
        }
    }
    
    /**
     * Update order amounts and status after payment
     */
    private void updateOrderAfterPayment(SalesOrder order) {
        // Recalculate paid amount from all transactions
        BigDecimal totalPaid = paymentRepository.calculateTotalPaidAmount(order.getId());
        BigDecimal totalRefunded = paymentRepository.calculateTotalRefundedAmount(order.getId());
        
        order.setPaidAmount(totalPaid.subtract(totalRefunded));
        order.calculateRemainingAmount();
        
        // Update payment status
        updatePaymentStatus(order);
        
        // Auto-update order status based on payment status
        if (order.getPaymentStatus() == PaymentStatus.FULLY_PAID) {
            // Fully paid → Auto transition to PAID status
            if (order.getOrderStatus() != OrderStatus.PAID && 
                order.getOrderStatus() != OrderStatus.COMPLETED &&
                order.getOrderStatus() != OrderStatus.CANCELLED) {
                order.setOrderStatus(OrderStatus.PAID);
                log.info("Order {} auto-updated to PAID status (fully paid)", order.getOrderNumber());
                
                // Update inventory car status to SOLD
                InventoryCar car = order.getInventoryCar();
                if (car != null && car.getSaleStatus() != SaleStatus.SOLD) {
                    car.setSaleStatus(SaleStatus.SOLD);
                    log.info("Inventory car {} updated to SOLD status", car.getVin());
                }
            }
        } else if (order.getPaymentStatus() == PaymentStatus.PARTIAL_PAID) {
            // Partial paid (has deposit) → Auto transition to DEPOSIT_PAID
            if (order.getOrderStatus() == OrderStatus.PENDING_DEPOSIT || 
                order.getOrderStatus() == OrderStatus.DRAFT) {
                order.setOrderStatus(OrderStatus.DEPOSIT_PAID);
                log.info("Order {} auto-updated to DEPOSIT_PAID status (deposit received)", order.getOrderNumber());
                
                // Reserve the car when deposit is received
                InventoryCar car = order.getInventoryCar();
                if (car != null && car.getSaleStatus() == SaleStatus.AVAILABLE) {
                    car.setSaleStatus(SaleStatus.RESERVED);
                    log.info("Inventory car {} updated to RESERVED status", car.getVin());
                }
            }
        }
        
        salesOrderRepository.save(order);
    }
    
    /**
     * Update payment status based on paid amount
     */
    private void updatePaymentStatus(SalesOrder order) {
        BigDecimal paid = order.getPaidAmount();
        BigDecimal total = order.getTotalPrice();
        
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        } else if (paid.compareTo(total) >= 0) {
            order.setPaymentStatus(PaymentStatus.FULLY_PAID);
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIAL_PAID);
        }
    }
}
