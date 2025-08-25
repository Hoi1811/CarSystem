package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionRequestDTO;
import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.AiSuggestionService;

@RestApiV1
@RequiredArgsConstructor
@Log4j2
public class AiAdviseController {

    private final AiSuggestionService aiSuggestionService;

    @PostMapping(Endpoint.V1.AI.SUGGEST)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getCarSuggestion(
            @RequestBody SuggestionRequestDTO request) {

        log.info("Received AI suggestion request for car: [{}]", request.carName());

        // Gọi service để xử lý và nhận về GlobalResponseDTO đã được đóng gói sẵn
        GlobalResponseDTO<NoPaginatedMeta, SuggestionResponseDTO> response =
                aiSuggestionService.getSuggestions(request);

        log.info("Finished processing AI suggestion for car: [{}]. Status: {}",
                request.carName(), response.meta().status());

        // Kiểm tra trạng thái từ service và trả về HTTP status tương ứng
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        } else {
            // Service đã xử lý lỗi và đóng gói thông báo, controller chỉ cần trả về badRequest
            return ResponseEntity.badRequest().body(response);
        }
    }
}