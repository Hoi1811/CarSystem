package web.car_system.Car_Service.domain.dto.ai_suggestion_request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO: trả cả markdown và html.
 * - Dùng @Builder để dễ khởi tạo trong service.
 * - @JsonInclude NON_NULL để không trả các trường null trong JSON.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestionResponseDTO {
    private String markdown;
    private String html;
}
