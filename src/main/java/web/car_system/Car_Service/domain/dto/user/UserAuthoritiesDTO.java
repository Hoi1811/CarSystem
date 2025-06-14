package web.car_system.Car_Service.domain.dto.user;

import java.util.List;

public record UserAuthoritiesDTO(
        List<String> roles,
        List<String> permissions
) {
}
