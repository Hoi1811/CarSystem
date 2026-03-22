package web.car_system.Car_Service.domain.dto.regional_fee;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new RegionalFee entry.
 * Intentionally excludes the {@code id} field to prevent client-supplied IDs.
 */
@Data
public class CreateRegionalFeeRequest {

    @NotEmpty(message = "Tên tỉnh/thành phố không được để trống")
    private String provinceCity;

    @NotNull(message = "Tỷ lệ phí trước bạ không được để trống")
    @PositiveOrZero(message = "Tỷ lệ phí trước bạ phải >= 0")
    private BigDecimal registrationFeeRate;

    @NotNull(message = "Phí biển số không được để trống")
    @PositiveOrZero(message = "Phí biển số phải >= 0")
    private BigDecimal licensePlateFee;

    @NotNull(message = "Phí sử dụng đường bộ không được để trống")
    @PositiveOrZero(message = "Phí sử dụng đường bộ phải >= 0")
    private BigDecimal roadUsageFee;

    @NotNull(message = "Phí đăng kiểm không được để trống")
    @PositiveOrZero(message = "Phí đăng kiểm phải >= 0")
    private BigDecimal inspectionFee;

    @NotNull(message = "Phí bảo hiểm TNDS không được để trống")
    @PositiveOrZero(message = "Phí bảo hiểm TNDS phải >= 0")
    private BigDecimal civilLiabilityInsuranceFee;
}
