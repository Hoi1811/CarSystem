package web.car_system.Car_Service.domain.dto.comparison;

import lombok.Builder;
import java.util.List;

@Builder
public record AttributeComparisonDTO(
        String attributeName,
        String unit, // Thêm đơn vị để hiển thị cho tiện
        List<AttributeValueComparisonDTO> comparedValues
) {}