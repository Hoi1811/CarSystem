package web.car_system.Car_Service.domain.dto.inventory_car;

import lombok.Data;
import web.car_system.Car_Service.domain.entity.CarCondition;
import web.car_system.Car_Service.domain.entity.SaleStatus;

import java.math.BigDecimal;

@Data
public class CreateInventoryCarRequest {
    // ID của mẫu xe để liên kết
    private Integer carId;

    // Thông tin cụ thể cho chiếc xe này
    private BigDecimal price;
    private String color;
    private String vin;
    private CarCondition conditionType;
    private SaleStatus saleStatus;
    private Integer mileage;
    private Integer yearOfManufacture;
    private String notes;
}