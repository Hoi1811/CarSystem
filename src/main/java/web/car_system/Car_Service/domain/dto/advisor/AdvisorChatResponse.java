package web.car_system.Car_Service.domain.dto.advisor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Trả cả markdown answer + list xe đã retrieve để FE render card.
 */
public record AdvisorChatResponse(
        String answer,
        String answerHtml,
        List<RetrievedCar> retrievedCars,
        Meta meta
) {

    public record RetrievedCar(
            Integer carId,
            String name,
            String model,
            Integer year,
            BigDecimal price,
            String thumbnail,
            String segmentName,
            String manufacturerName,
            double score
    ) {}

    public record Meta(
            String sessionId,
            long latencyMs,
            int retrievedCount,
            String chatModel,
            String embeddingModel
    ) {}
}
