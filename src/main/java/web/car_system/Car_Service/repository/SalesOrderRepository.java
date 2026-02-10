package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.SalesOrder;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {
    
    /**
     * Find order by order number
     */
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by customer phone
     */
    List<SalesOrder> findByCustomerPhone(String customerPhone);
    
    /**
     * Find orders by sales staff
     */
    List<SalesOrder> findBySalesStaffUserId(Long salesStaffUserId);
    
    /**
     * Find orders by status
     */
    List<SalesOrder> findByOrderStatus(OrderStatus orderStatus);
    
    /**
     * Check if inventory car has active order
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM SalesOrder o " +
           "WHERE o.inventoryCar = :car " +
           "AND o.orderStatus IN :activeStatuses")
    boolean existsByInventoryCarAndOrderStatusIn(
        @Param("car") InventoryCar car,
        @Param("activeStatuses") List<OrderStatus> activeStatuses
    );
    
    /**
     * Find active order by inventory car
     */
    @Query("SELECT o FROM SalesOrder o " +
           "WHERE o.inventoryCar.id = :carId " +
           "AND o.orderStatus NOT IN ('COMPLETED', 'CANCELLED')")
    Optional<SalesOrder> findActiveOrderByInventoryCarId(@Param("carId") Long carId);
    
    /**
     * Check if order number already exists
     */
    boolean existsByOrderNumber(String orderNumber);
    
    // ============================================
    // ANALYTICS QUERIES (Optimized DB Aggregation)
    // ============================================
    
    /**
     * Get monthly revenue for a specific year
     * Returns aggregated data for charts
     */
    @Query("SELECT MONTH(o.orderDate) as month, SUM(o.totalPrice) as amount " +
           "FROM SalesOrder o " +
           "WHERE YEAR(o.orderDate) = :year " +
           "AND o.orderStatus = 'COMPLETED' " +
           "GROUP BY MONTH(o.orderDate) " +
           "ORDER BY MONTH(o.orderDate)")
    List<Object[]> findMonthlyRevenueByYear(@Param("year") int year);
    
    /**
     * Get order count distribution by status
     * For pie/doughnut charts
     */
    @Query("SELECT o.orderStatus, COUNT(o) " +
           "FROM SalesOrder o " +
           "GROUP BY o.orderStatus")
    List<Object[]> countOrdersByStatus();
    
    /**
     * Get total revenue (completed orders only)
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) " +
           "FROM SalesOrder o " +
           "WHERE o.orderStatus = 'COMPLETED'")
    java.math.BigDecimal getTotalRevenue();
    
    /**
     * Get total paid amount across all orders
     */
    @Query("SELECT COALESCE(SUM(o.paidAmount), 0) FROM SalesOrder o")
    java.math.BigDecimal getTotalPaidAmount();
    
    /**
     * Count orders by status
     */
    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE o.orderStatus = :status")
    long countByOrderStatus(@Param("status") OrderStatus status);
    
    /**
     * Get top performing sales staff
     * Returns: staffId, staffName, orderCount, totalRevenue
     */
    @Query("SELECT o.salesStaff.userId, o.salesStaff.fullName, " +
           "COUNT(o), SUM(CASE WHEN o.orderStatus = 'COMPLETED' THEN o.totalPrice ELSE 0 END) " +
           "FROM SalesOrder o " +
           "WHERE o.salesStaff IS NOT NULL " +
           "GROUP BY o.salesStaff.userId, o.salesStaff.fullName " +
           "ORDER BY SUM(CASE WHEN o.orderStatus = 'COMPLETED' THEN o.totalPrice ELSE 0 END) DESC")
    List<Object[]> findTopSalesStaff(org.springframework.data.domain.Pageable pageable);
}
