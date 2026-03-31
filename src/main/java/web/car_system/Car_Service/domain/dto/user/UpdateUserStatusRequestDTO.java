package web.car_system.Car_Service.domain.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequestDTO(
        @NotNull(message = "Trạng thái kích hoạt không được để trống")
        Boolean isEnabled
) {}
