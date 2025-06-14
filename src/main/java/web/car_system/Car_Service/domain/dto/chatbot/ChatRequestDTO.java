package web.car_system.Car_Service.domain.dto.chatbot;

public record ChatRequestDTO(
        String sessionId,
        String message
) {
}
