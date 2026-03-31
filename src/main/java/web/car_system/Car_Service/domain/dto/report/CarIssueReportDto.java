package web.car_system.Car_Service.domain.dto.report;

import web.car_system.Car_Service.domain.entity.CarIssueReport;

import java.sql.Timestamp;

public record CarIssueReportDto(
        Integer reportId,
        Integer carId,
        String carName,
        Long reporterId,
        String reporterName,
        String description,
        CarIssueReport.ReportStatus reportStatus,
        Timestamp createdAt
) {
}
