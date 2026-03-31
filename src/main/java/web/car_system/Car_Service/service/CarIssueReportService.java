package web.car_system.Car_Service.service;

import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.report.AddCarIssueReportDto;
import web.car_system.Car_Service.domain.dto.report.CarIssueReportDto;
import web.car_system.Car_Service.domain.entity.CarIssueReport;

import java.util.List;

public interface CarIssueReportService {
    GlobalResponseDTO<?, CarIssueReportDto> createReport(AddCarIssueReportDto request, Long userId);
    GlobalResponseDTO<PaginatedMeta, List<CarIssueReportDto>> getAllReports(Pageable pageable);
    GlobalResponseDTO<PaginatedMeta, List<CarIssueReportDto>> getReportsByReportStatus(CarIssueReport.ReportStatus reportStatus, Pageable pageable);
    GlobalResponseDTO<?, CarIssueReportDto> updateReportStatus(Integer reportId, CarIssueReport.ReportStatus newStatus);
}
