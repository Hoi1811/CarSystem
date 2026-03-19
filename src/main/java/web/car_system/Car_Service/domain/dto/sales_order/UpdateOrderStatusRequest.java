package web.car_system.Car_Service.domain.dto.sales_order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import web.car_system.Car_Service.domain.entity.OrderStatus;

@Getter
public class UpdateOrderStatusRequest {

    @NotNull(message = "status không được để trống")
    private OrderStatus status;

    private String reason;
}
