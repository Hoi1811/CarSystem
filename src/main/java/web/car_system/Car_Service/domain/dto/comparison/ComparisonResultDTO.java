package web.car_system.Car_Service.domain.dto.comparison;

import lombok.Builder;
import java.util.List;

@Builder
public record ComparisonResultDTO(
        // Danh sách các xe được so sánh và điểm số
        List<CarComparisonProfileDTO> carProfiles,

        // Kết quả so sánh chi tiết, được nhóm theo từng specification
        List<SpecificationComparisonDTO> specificationComparisons
) {}