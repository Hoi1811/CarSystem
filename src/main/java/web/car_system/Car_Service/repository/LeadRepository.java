package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.Lead;
import web.car_system.Car_Service.domain.entity.LeadStatus;
import web.car_system.Car_Service.domain.entity.User;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    // Lọc lead theo trạng thái (ví dụ: chỉ hiển thị lead mới)
    Page<Lead> findAllByStatus(LeadStatus status, Pageable pageable);

    // Lọc lead theo nhân viên được gán
    Page<Lead> findAllByAssignee(User assignee, Pageable pageable);
}
