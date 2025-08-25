package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionRequestDTO;
import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;

public interface AiSuggestionService {
    public abstract GlobalResponseDTO<NoPaginatedMeta, SuggestionResponseDTO> getSuggestions(SuggestionRequestDTO request);
}
