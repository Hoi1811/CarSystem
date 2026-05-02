package web.car_system.Car_Service.domain.dto.analytics;

import java.math.BigDecimal;

public record TopSalesStaffProjection(
        Long staffId,
        String staffName,
        Long totalOrders,
        BigDecimal totalRevenue
) {
}
