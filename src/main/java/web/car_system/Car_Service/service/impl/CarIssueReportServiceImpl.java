package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Pagination;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.report.AddCarIssueReportDto;
import web.car_system.Car_Service.domain.dto.report.CarIssueReportDto;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarIssueReport;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.repository.CarIssueReportRepository;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.CarIssueReportService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarIssueReportServiceImpl implements CarIssueReportService {

    private final CarIssueReportRepository reportRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Override
    public GlobalResponseDTO<?, CarIssueReportDto> createReport(AddCarIssueReportDto request, Long userId) {
        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy xe với ID " + request.carId()));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        CarIssueReport report = CarIssueReport.builder()
                .car(car)
                .reporter(user)
                .description(request.description())
                .reportStatus(CarIssueReport.ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);

        return GlobalResponseDTO.<NoPaginatedMeta, CarIssueReportDto>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Báo cáo đã được gửi thành công")
                        .build())
                .data(mapToDto(report))
                .build();
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarIssueReportDto>> getAllReports(Pageable pageable) {
        Page<CarIssueReport> page = reportRepository.findAll(pageable);
        return buildPaginatedResponse(page, "Lấy danh sách báo cáo lỗi xe thành công");
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarIssueReportDto>> getReportsByReportStatus(CarIssueReport.ReportStatus reportStatus, Pageable pageable) {
        Page<CarIssueReport> page = reportRepository.findByReportStatus(reportStatus, pageable);
        return buildPaginatedResponse(page, "Lấy danh sách báo cáo trạng thái " + reportStatus + " thành công");
    }

    @Override
    public GlobalResponseDTO<?, CarIssueReportDto> updateReportStatus(Integer reportId, CarIssueReport.ReportStatus newStatus) {
        CarIssueReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy báo cáo id " + reportId));
        
        report.setReportStatus(newStatus);
        report = reportRepository.save(report);

        return GlobalResponseDTO.<NoPaginatedMeta, CarIssueReportDto>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Đã cập nhật trạng thái báo cáo")
                        .build())
                .data(mapToDto(report))
                .build();
    }

    private CarIssueReportDto mapToDto(CarIssueReport entity) {
        return new CarIssueReportDto(
                entity.getReportId(),
                entity.getCar().getCarId(),
                entity.getCar().getName() + " " + entity.getCar().getModel(),
                entity.getReporter().getUserId(),
                entity.getReporter().getFullName(),
                entity.getDescription(),
                entity.getReportStatus(),
                entity.getCreatedAt()
        );
    }

    private GlobalResponseDTO<PaginatedMeta, List<CarIssueReportDto>> buildPaginatedResponse(Page<CarIssueReport> page, String message) {
        List<CarIssueReportDto> dtos = page.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        Pagination pagination = Pagination.builder()
                .pageIndex(page.getNumber())
                .pageSize((short) page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
        return GlobalResponseDTO.<PaginatedMeta, List<CarIssueReportDto>>builder()
                .meta(PaginatedMeta.builder().status(Status.SUCCESS).message(message).pagination(pagination).build())
                .data(dtos).build();
    }
}
