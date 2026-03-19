package web.car_system.Car_Service.domain.dto.inventory_car;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import web.car_system.Car_Service.domain.entity.SaleStatus;

@Getter
public class UpdateSaleStatusRequest {
    @NotNull(message = "saleStatus không được để trống")
    private SaleStatus saleStatus;
}
