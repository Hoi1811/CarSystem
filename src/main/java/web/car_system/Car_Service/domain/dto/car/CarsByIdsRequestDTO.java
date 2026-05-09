package web.car_system.Car_Service.domain.dto.car;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CarsByIdsRequestDTO(
        @NotNull List<Integer> ids,
        Integer limit
) {
}
