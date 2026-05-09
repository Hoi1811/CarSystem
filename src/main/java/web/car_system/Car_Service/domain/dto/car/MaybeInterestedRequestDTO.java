package web.car_system.Car_Service.domain.dto.car;

import java.util.List;

public record MaybeInterestedRequestDTO(
        List<Integer> seedCarIds,
        Integer limit
) {
}
