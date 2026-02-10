package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.car_system.Car_Service.domain.entity.AppointmentStatus;
import web.car_system.Car_Service.domain.entity.TestDriveAppointment;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveAppointmentRepository extends JpaRepository<TestDriveAppointment, Long>, JpaSpecificationExecutor<TestDriveAppointment> {
    // Lọc lịch hẹn theo trạng thái
    Page<TestDriveAppointment> findAllByStatus(AppointmentStatus status, Pageable pageable);

    // Lấy các lịch hẹn trong một khoảng thời gian (hữu ích cho giao diện Lịch)
    List<TestDriveAppointment> findAllByConfirmedDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
