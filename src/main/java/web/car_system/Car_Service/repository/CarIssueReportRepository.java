package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.CarIssueReport;

@Repository
public interface CarIssueReportRepository extends JpaRepository<CarIssueReport, Integer> {
    Page<CarIssueReport> findByReportStatus(CarIssueReport.ReportStatus reportStatus, Pageable pageable);
    Page<CarIssueReport> findByCarCarId(Integer carId, Pageable pageable);
}
