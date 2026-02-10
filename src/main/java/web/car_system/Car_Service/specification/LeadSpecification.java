package web.car_system.Car_Service.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import web.car_system.Car_Service.domain.dto.lead.LeadFilterRequest;
import web.car_system.Car_Service.domain.entity.Lead;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification cho Lead entity
 * Xây dựng dynamic queries dựa trên LeadFilterRequest
 */
public class LeadSpecification {

    /**
     * Build Specification từ filter request
     * @param filter LeadFilterRequest chứa các tiêu chí tìm kiếm
     * @return Specification<Lead> để dùng trong repository.findAll()
     */
    public static Specification<Lead> buildSpec(LeadFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search (customerName, phoneNumber, email)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerName")), 
                    pattern
                );
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("phoneNumber"), 
                    pattern
                );
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    pattern
                );
                
                // OR condition: match any of the fields
                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate, emailPredicate));
            }

            // 2. Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leadStatus"), filter.getStatus()));
            }

            // 3. Filter by assignee
            if (filter.getAssigneeId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("assignee").get("id"), 
                    filter.getAssigneeId()
                ));
            }

            // 4. Filter by date range (createdAt)
            if (filter.getFromDate() != null) {
                LocalDateTime startOfDay = filter.getFromDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), 
                    startOfDay
                ));
            }
            
            if (filter.getToDate() != null) {
                LocalDateTime endOfDay = filter.getToDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), 
                    endOfDay
                ));
            }

            // 5. Filter by request type
            if (filter.getRequestType() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("requestType"), 
                    filter.getRequestType()
                ));
            }

            // 6. Filter by interested car
            if (filter.getInventoryCarId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("interestedCar").get("id"), 
                    filter.getInventoryCarId()
                ));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
