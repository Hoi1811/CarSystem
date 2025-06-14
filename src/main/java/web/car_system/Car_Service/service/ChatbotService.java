package web.car_system.Car_Service.service;

import reactor.core.publisher.Mono;
import web.car_system.Car_Service.domain.dto.chatbot.ChatRequestDTO;
import web.car_system.Car_Service.domain.dto.chatbot.ChatResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;

public interface ChatbotService {
    Mono<GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO>> getAiReply(ChatRequestDTO chatRequestDTO);
}
