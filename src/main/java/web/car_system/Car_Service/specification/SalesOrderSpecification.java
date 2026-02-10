package web.car_system.Car_Service.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import web.car_system.Car_Service.domain.dto.sales_order.OrderFilterRequest;
import web.car_system.Car_Service.domain.entity.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderSpecification {
    
    /**
     * Build dynamic specification from filter request
     */
    public static Specification<SalesOrder> filterOrders(OrderFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Keyword search (customer name, phone, order number)
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerName")), keyword);
                Predicate phoneMatch = criteriaBuilder.like(
                    root.get("customerPhone"), "%" + filter.getKeyword().trim() + "%");
                Predicate orderNumberMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("orderNumber")), keyword);
                
                predicates.add(criteriaBuilder.or(nameMatch, phoneMatch, orderNumberMatch));
            }
            
            // Filter by order status
            if (filter.getOrderStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("orderStatus"), filter.getOrderStatus()));
            }
            
            // Filter by payment status
            if (filter.getPaymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("paymentStatus"), filter.getPaymentStatus()));
            }
            
            // Filter by sales staff
            if (filter.getSalesStaffId() != null) {
                Join<SalesOrder, User> staffJoin = root.join("salesStaff", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                    staffJoin.get("userId"), filter.getSalesStaffId()));
            }
            
            // Filter by date range
            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("orderDate"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("orderDate"), filter.getToDate()));
            }
            
            // Filter by inventory car
            if (filter.getInventoryCarId() != null) {
                Join<SalesOrder, InventoryCar> carJoin = root.join("inventoryCar", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                    carJoin.get("id"), filter.getInventoryCarId()));
            }
            
            // Filter by lead
            if (filter.getLeadId() != null) {
                Join<SalesOrder, Lead> leadJoin = root.join("lead", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                    leadJoin.get("id"), filter.getLeadId()));
            }
            
            // Filter by price range
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("totalPrice"), BigDecimal.valueOf(filter.getMinPrice())));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("totalPrice"), BigDecimal.valueOf(filter.getMaxPrice())));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
