package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.OrderStatusHistory;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    /**
     * Find all status changes for an order, ordered by time descending
     */
    List<OrderStatusHistory> findBySalesOrderIdOrderByChangedAtDesc(Long salesOrderId);
}
