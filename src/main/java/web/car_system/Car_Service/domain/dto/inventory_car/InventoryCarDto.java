package web.car_system.Car_Service.domain.dto.inventory_car;

import lombok.Builder;
import lombok.Data;
import web.car_system.Car_Service.domain.entity.CarCondition;
import web.car_system.Car_Service.domain.entity.SaleStatus;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryCarDto {
    // Thông tin từ InventoryCar
    private Long id;
    private BigDecimal price;
    private String color;
    private String vin;
    private CarCondition conditionType;
    private SaleStatus saleStatus;
    private Integer mileage;
    private String notes;

    // Thông tin từ Car (mẫu xe)
    private Integer carId;
    private String carName;
    private String carModel;
    private Integer carYear;
    private String carThumbnail;

    // Thông tin từ Manufacturer
    private String manufacturerName;

    // Thông tin Showroom (chi nhánh)
    private Long showroomId;
    private String showroomName;
    private String showroomCode;
    private String showroomAddress;
    private String showroomPhone;
    private java.math.BigDecimal showroomLatitude;
    private java.math.BigDecimal showroomLongitude;
}
