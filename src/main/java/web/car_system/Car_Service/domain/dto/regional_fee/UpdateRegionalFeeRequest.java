package web.car_system.Car_Service.domain.dto.regional_fee;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Request DTO for updating an existing RegionalFee entry.
 * All fields are optional — only non-null values will be applied.
 * The {@code id} field is intentionally excluded; ID comes from the path variable.
 */
@Data
public class UpdateRegionalFeeRequest {

    private String provinceCity;

    @PositiveOrZero(message = "Tỷ lệ phí trước bạ phải >= 0")
    private BigDecimal registrationFeeRate;

    @PositiveOrZero(message = "Phí biển số phải >= 0")
    private BigDecimal licensePlateFee;

    @PositiveOrZero(message = "Phí sử dụng đường bộ phải >= 0")
    private BigDecimal roadUsageFee;

    @PositiveOrZero(message = "Phí đăng kiểm phải >= 0")
    private BigDecimal inspectionFee;

    @PositiveOrZero(message = "Phí bảo hiểm TNDS phải >= 0")
    private BigDecimal civilLiabilityInsuranceFee;
}
