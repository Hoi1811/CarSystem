package web.car_system.Car_Service.domain.dto.auth;

public record RegisterRequestDTO(
        String username, String password, String email, boolean rememberMe
) {
}
