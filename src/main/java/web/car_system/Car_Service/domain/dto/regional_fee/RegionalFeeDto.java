package web.car_system.Car_Service.domain.dto.regional_fee;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RegionalFeeDto {
    private Long id;

    @NotEmpty
    private String provinceCity;

    @NotNull
    private BigDecimal registrationFeeRate;

    @NotNull
    private BigDecimal licensePlateFee;

    @NotNull
    private BigDecimal roadUsageFee;

    @NotNull
    private BigDecimal inspectionFee;

    @NotNull
    private BigDecimal civilLiabilityInsuranceFee;
}
