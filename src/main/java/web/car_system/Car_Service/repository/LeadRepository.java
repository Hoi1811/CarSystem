package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.Lead;
import web.car_system.Car_Service.domain.entity.LeadStatus;
import web.car_system.Car_Service.domain.entity.User;

import java.time.LocalDateTime;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {
    // Lọc lead theo trạng thái (ví dụ: chỉ hiển thị lead mới)
    Page<Lead> findAllByLeadStatus(LeadStatus leadStatus, Pageable pageable);

    // Lọc lead theo nhân viên được gán
    Page<Lead> findAllByAssignee(User assignee, Pageable pageable);
    
    // ============================================
    // ANALYTICS QUERIES
    // ============================================
    
    /**
     * Count total leads
     */
    @Query("SELECT COUNT(l) FROM Lead l")
    long countTotalLeads();
    
    /**
     * Count leads by status
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.leadStatus = :leadStatus")
    long countByLeadStatus(@Param("leadStatus") LeadStatus leadStatus);
    
    /**
     * Count new leads since a specific date
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.createdAt >= :startDate")
    long countNewLeadsSince(@Param("startDate") LocalDateTime startDate);
}
