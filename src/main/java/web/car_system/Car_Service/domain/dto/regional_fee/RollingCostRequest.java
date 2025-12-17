package web.car_system.Car_Service.domain.dto.regional_fee;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RollingCostRequest {
    @NotEmpty(message = "Vui lòng chọn Tỉnh/Thành phố")
    private String provinceCity;
}
