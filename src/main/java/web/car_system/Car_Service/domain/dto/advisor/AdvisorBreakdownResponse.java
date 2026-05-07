package web.car_system.Car_Service.domain.dto.advisor;

import java.util.List;

/**
 * Endpoint debug: trả nguyên trình tự RAG để chiếu cho hội đồng/thuyết trình
 *  step1: query gốc
 *  step2: 5 số đầu của query embedding
 *  step3: top-K xe + score
 *  step4: prompt cuối gửi LLM (truncate)
 *  step5: response của LLM
 */
public record AdvisorBreakdownResponse(
        String step1Query,
        List<Float> step2QueryEmbeddingPreview,
        List<ScoredCarPreview> step3RetrievedCars,
        String step4FinalPrompt,
        String step5LlmResponse,
        long latencyMs
) {
    public record ScoredCarPreview(
            Integer carId,
            String name,
            String model,
            double score
    ) {}
}
