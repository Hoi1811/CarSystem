package web.car_system.Car_Service.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import web.car_system.Car_Service.domain.dto.test_drive.TestDriveFilterRequest;
import web.car_system.Car_Service.domain.entity.TestDriveAppointment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification cho TestDriveAppointment entity
 * Xây dựng dynamic queries dựa trên TestDriveFilterRequest
 */
public class TestDriveSpecification {

    /**
     * Build Specification từ filter request
     * @param filter TestDriveFilterRequest chứa các tiêu chí tìm kiếm
     * @return Specification<TestDriveAppointment> để dùng trong repository.findAll()
     */
    public static Specification<TestDriveAppointment> buildSpec(TestDriveFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search (customerName, customerPhone, customerEmail)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerName")), 
                    pattern
                );
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("customerPhone"), 
                    pattern
                );
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerEmail")), 
                    pattern
                );
                
                // OR condition: match any of the fields
                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate, emailPredicate));
            }

            // 2. Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // 3. Filter by appointment date range
            if (filter.getFromDate() != null) {
                LocalDateTime startOfDay = filter.getFromDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("appointmentDate"), 
                    startOfDay
                ));
            }
            
            if (filter.getToDate() != null) {
                LocalDateTime endOfDay = filter.getToDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("appointmentDate"), 
                    endOfDay
                ));
            }

            // 4. Filter by inventory car
            if (filter.getInventoryCarId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("inventoryCar").get("id"), 
                    filter.getInventoryCarId()
                ));
            }

            // 5. Filter by assigned user (staff member)
            if (filter.getAssignedUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("assignedUser").get("id"), 
                    filter.getAssignedUserId()
                ));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
