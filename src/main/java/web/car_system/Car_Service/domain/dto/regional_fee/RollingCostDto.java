package web.car_system.Car_Service.domain.dto.regional_fee;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RollingCostDto {
    private BigDecimal carPrice; // Giá niêm yết của xe

    // Các loại phí chi tiết
    private BigDecimal registrationFee; // Phí trước bạ
    private BigDecimal licensePlateFee; // Phí biển số
    private BigDecimal roadUsageFee; // Phí đường bộ
    private BigDecimal inspectionFee; // Phí đăng kiểm
    private BigDecimal civilLiabilityInsuranceFee; // Phí bảo hiểm TNDS

    // Kết quả cuối cùng
    private BigDecimal totalRollingCost; // TỔNG CHI PHÍ LĂN BÁNH
}
