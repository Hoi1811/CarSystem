package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.payment.AddPaymentRequest;
import web.car_system.Car_Service.domain.dto.payment.PaymentTransactionDto;

import java.util.List;

public interface PaymentTransactionService {
    
    /**
     * Add a payment to an order
     * - Validates payment amount
     * - Updates order paid amount
     * - Auto-updates payment status
     * - Auto-updates order status if fully paid
     * 
     * @param orderId Sales order ID
     * @param request Payment details
     * @return Created payment transaction
     */
    PaymentTransactionDto addPayment(Long orderId, AddPaymentRequest request);
    
    /**
     * Get all payments for an order
     * 
     * @param orderId Sales order ID
     * @return List of payment transactions ordered by date desc
     */
    List<PaymentTransactionDto> getPaymentHistory(Long orderId);
    
    /**
     * Get payment by ID
     * 
     * @param id Payment transaction ID
     * @return Payment details
     */
    PaymentTransactionDto getPaymentById(Long id);
}
