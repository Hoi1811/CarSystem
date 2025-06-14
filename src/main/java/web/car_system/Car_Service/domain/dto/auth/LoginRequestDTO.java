package web.car_system.Car_Service.domain.dto.auth;

public record LoginRequestDTO(
        String username, String password, boolean rememberMe
) {
}
