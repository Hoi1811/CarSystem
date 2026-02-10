package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    /**
     * Find all payments for a specific order
     */
    List<PaymentTransaction> findBySalesOrderIdOrderByTransactionDateDesc(Long salesOrderId);
    
    /**
     * Calculate total paid amount for an order
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p " +
           "WHERE p.salesOrder.id = :orderId " +
           "AND p.paymentType IN ('DEPOSIT', 'FINAL_PAYMENT')")
    BigDecimal calculateTotalPaidAmount(@Param("orderId") Long orderId);
    
    /**
     * Calculate total refunded amount for an order
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p " +
           "WHERE p.salesOrder.id = :orderId " +
           "AND p.paymentType = 'REFUND'")
    BigDecimal calculateTotalRefundedAmount(@Param("orderId") Long orderId);
}
