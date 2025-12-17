package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.ContactLog;
import web.car_system.Car_Service.domain.entity.Lead;
import web.car_system.Car_Service.domain.entity.TestDriveAppointment;

import java.util.List;

public interface ContactLogRepository extends JpaRepository<ContactLog, Long> {

    // Lấy toàn bộ lịch sử tương tác của một Lead, sắp xếp mới nhất lên đầu
    List<ContactLogRepository> findByLeadOrderByLogDateTimeDesc(Lead lead);

    // Lấy toàn bộ lịch sử tương tác của một Lịch hẹn
    List<ContactLogRepository> findByAppointmentOrderByLogDateTimeDesc(TestDriveAppointment appointment);
}