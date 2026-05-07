package web.car_system.Car_Service.domain.dto.advisor;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Body cho POST {baseUrl}/models/{model}:embedContent?key=...
 * Spec: https://ai.google.dev/api/embeddings#method:-models.embedcontent
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiEmbedRequest(
        String model,
        Content content,
        String taskType,
        Integer outputDimensionality
) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public static GeminiEmbedRequest of(String model, String text, EmbeddingTaskType taskType, int dimensions) {
        return new GeminiEmbedRequest(
                "models/" + model,
                new Content(List.of(new Part(text))),
                taskType.name(),
                dimensions
        );
    }
}
