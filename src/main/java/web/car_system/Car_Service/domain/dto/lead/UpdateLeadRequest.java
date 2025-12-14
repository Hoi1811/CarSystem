package web.car_system.Car_Service.domain.dto.lead;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.LeadStatus;

@Data
public class UpdateLeadRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private LeadStatus leadStatus; // Trạng thái mới của Lead

    private Long assigneeId; // ID của nhân viên mới được gán
}