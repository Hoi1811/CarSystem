package web.car_system.Car_Service.domain.dto.inventory_car;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.CarCondition;
import web.car_system.Car_Service.domain.entity.SaleStatus;

import java.math.BigDecimal;

@Data
public class CreateInventoryCarRequest {
    @NotNull(message = "carId không được để trống")
    private Integer carId;

    @NotNull(message = "price không được để trống")
    @PositiveOrZero(message = "price phải là số không âm")
    private BigDecimal price;

    private String color;

    @Size(max = 17, message = "VIN không được vượt quá 17 ký tự")
    private String vin;

    @NotNull(message = "conditionType không được để trống")
    private CarCondition conditionType;

    @NotNull(message = "saleStatus không được để trống")
    private SaleStatus saleStatus;

    @PositiveOrZero(message = "mileage phải là số không âm")
    private Integer mileage;

    private Integer yearOfManufacture;
    private String notes;
}