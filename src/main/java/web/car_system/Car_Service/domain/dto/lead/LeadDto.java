package web.car_system.Car_Service.domain.dto.lead;

import lombok.Builder;
import lombok.Data;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.TestDriveAppointmentDto;
import web.car_system.Car_Service.domain.entity.LeadRequestType;
import web.car_system.Car_Service.domain.entity.LeadStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class LeadDto {
    private Long id;

    // Thông tin khách hàng & yêu cầu
    private String customerName;
    private String phoneNumber;
    private String email;
    private LeadRequestType requestType;
    private LeadStatus leadStatus;
    private String notes;
    private LocalDateTime createdAt;

    // Thông tin tóm tắt về các đối tượng liên quan (có thể null)
    private TestDriveAppointmentDto.CarSummaryDto carSummary;
    private TestDriveAppointmentDto.UserSummaryDto assignee; // Tái sử dụng UserSummaryDto
}