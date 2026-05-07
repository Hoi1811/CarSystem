package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorBreakdownResponse;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatRequest;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatResponse;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.advisor.EmbeddingPipelineService;
import web.car_system.Car_Service.service.advisor.RagAdvisorService;
import web.car_system.Car_Service.service.advisor.VectorSearchService;

import java.util.Map;

/**
 * REST endpoints cho AI Advisor.
 *
 * User:
 *  - POST /api/v1/ai-advisor/chat       → tư vấn (RAG)
 *  - POST /api/v1/ai-advisor/breakdown  → debug, show inner-working cho thuyết trình
 *
 * Admin (chưa gắn @PreAuthorize — sau khi tích hợp security thì gắn ROLE_ADMIN):
 *  - POST /api/v1/ai-advisor/admin/regenerate     → embed lại toàn bộ catalog
 *  - POST /api/v1/ai-advisor/admin/embed/{carId}  → embed 1 xe
 *  - POST /api/v1/ai-advisor/admin/reload-cache   → reload cache từ DB
 *  - GET  /api/v1/ai-advisor/admin/status         → kiểm tra số embedding đã load
 */
@RestApiV1
public class AiAdvisorController {

    private static final Logger log = LoggerFactory.getLogger(AiAdvisorController.class);

    private final RagAdvisorService ragAdvisorService;
    private final EmbeddingPipelineService pipelineService;
    private final VectorSearchService vectorSearchService;

    public AiAdvisorController(RagAdvisorService ragAdvisorService,
                               EmbeddingPipelineService pipelineService,
                               VectorSearchService vectorSearchService) {
        this.ragAdvisorService = ragAdvisorService;
        this.pipelineService = pipelineService;
        this.vectorSearchService = vectorSearchService;
    }

    @PostMapping(Endpoint.V1.AI_ADVISOR.CHAT)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, AdvisorChatResponse>> chat(
            @Valid @RequestBody AdvisorChatRequest request) {
        log.info("AI Advisor chat: query='{}', sessionId={}",
                truncate(request.query()), request.sessionId());
        AdvisorChatResponse response = ragAdvisorService.chat(request);
        return ResponseEntity.ok(success(response, "Tư vấn thành công"));
    }

    @PostMapping(Endpoint.V1.AI_ADVISOR.BREAKDOWN)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, AdvisorBreakdownResponse>> breakdown(
            @Valid @RequestBody AdvisorChatRequest request) {
        log.info("AI Advisor breakdown: query='{}'", truncate(request.query()));
        AdvisorBreakdownResponse response = ragAdvisorService.breakdown(request);
        return ResponseEntity.ok(success(response, "Breakdown OK"));
    }

    @PostMapping(Endpoint.V1.AI_ADVISOR.ADMIN_REGENERATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, EmbeddingPipelineService.EmbedStats>> regenerate(
            @RequestParam(name = "limit", required = false) Integer limit) {
        int effective = (limit == null || limit <= 0) ? Integer.MAX_VALUE : limit;
        log.info("Admin trigger: regenerate embeddings (limit={})",
                effective == Integer.MAX_VALUE ? "ALL" : effective);
        EmbeddingPipelineService.EmbedStats stats = pipelineService.regenerateAll(effective);
        return ResponseEntity.ok(success(stats, "Embedding pipeline finished"));
    }

    @PostMapping(Endpoint.V1.AI_ADVISOR.ADMIN_EMBED_CAR)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Map<String, Object>>> embedCar(
            @PathVariable Integer carId) {
        log.info("Admin trigger: embed single car {}", carId);
        pipelineService.embedSingleCar(carId);
        return ResponseEntity.ok(success(Map.of("carId", carId, "embedded", true), "Embedded"));
    }

    @PostMapping(Endpoint.V1.AI_ADVISOR.ADMIN_RELOAD_CACHE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Map<String, Object>>> reloadCache() {
        vectorSearchService.reloadCache();
        return ResponseEntity.ok(success(
                Map.of("cacheSize", vectorSearchService.cacheSize()),
                "Cache reloaded"));
    }

    @GetMapping(Endpoint.V1.AI_ADVISOR.ADMIN_STATUS)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Map<String, Object>>> status() {
        return ResponseEntity.ok(success(
                Map.of("cacheSize", vectorSearchService.cacheSize()),
                "OK"));
    }

    // ==================== Helpers ====================

    private static <T> GlobalResponseDTO<NoPaginatedMeta, T> success(T data, String message) {
        return GlobalResponseDTO.<NoPaginatedMeta, T>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message(message).build())
                .data(data)
                .build();
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() <= 100 ? s : s.substring(0, 100) + "...";
    }
}
