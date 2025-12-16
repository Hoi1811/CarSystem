package web.car_system.Car_Service.domain.dto.inventory_car;

import lombok.Getter;
import web.car_system.Car_Service.domain.entity.SaleStatus;
@Getter
public class UpdateSaleStatusRequest {
    private SaleStatus saleStatus;
}
