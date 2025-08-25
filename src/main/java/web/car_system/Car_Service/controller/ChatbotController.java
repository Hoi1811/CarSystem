package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.chatbot.ChatRequestDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.ChatbotService;

import java.time.Instant;

@RestApiV1
@RequiredArgsConstructor
@Log4j2
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping(Endpoint.V1.CHATBOT.CHAT)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getChatbotResponse(@RequestBody ChatRequestDTO request) {
        log.info("Received request for session [{}]. Processing with Virtual Thread...", request.sessionId());

        // Gọi service như bình thường. Không có async/await hay .thenApply
        GlobalResponseDTO<NoPaginatedMeta, ?> response = chatbotService.getAiReply(request);

        log.info("Finished processing session [{}].", request.sessionId());

        // Trả về kết quả
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
