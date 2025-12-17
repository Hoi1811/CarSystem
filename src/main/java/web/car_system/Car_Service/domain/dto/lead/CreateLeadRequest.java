package web.car_system.Car_Service.domain.dto.lead;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.LeadRequestType;

@Data
public class CreateLeadRequest {
    @NotEmpty(message = "Tên không được để trống")
    private String customerName;

    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String email;

    @NotNull(message = "Vui lòng chọn loại yêu cầu")
    private LeadRequestType requestType; // PRICE_QUOTE or INSTALLMENT

    private String notes;

    // Xe khách hàng quan tâm, có thể không có (hỏi chung chung)
    private Long inventoryCarId;
}
