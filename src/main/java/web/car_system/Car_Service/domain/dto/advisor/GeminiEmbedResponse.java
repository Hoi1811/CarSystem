package web.car_system.Car_Service.domain.dto.advisor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Response từ Gemini embedContent — chỉ map field `embedding.values` (list float).
 * Bỏ qua field khác để forward-compatible nếu Gemini thêm metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiEmbedResponse(Embedding embedding) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Embedding(List<Float> values) {}
}
