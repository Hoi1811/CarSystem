package web.car_system.Car_Service.domain.dto.ai_suggestion_request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO: giữ dạng record để ngắn gọn.
 * Spring + Jakarta Validation sẽ validate các trường khi controller dùng @Valid.
 */
public record SuggestionRequestDTO(
        @NotBlank(message = "Thiếu tên xe")
        String carName,

        @NotBlank(message = "Thiếu thông số kĩ thuật")
        String specificationsText) {
}
