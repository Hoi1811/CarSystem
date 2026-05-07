package web.car_system.Car_Service.service.advisor;

/**
 * Pipeline embedding cho catalog xe. Tách khỏi Scheduler để có thể trigger thủ công
 * (qua admin endpoint) hoặc test độc lập.
 *
 *  buildText → hash → if-changed → embed → save → reload-cache
 */
public interface EmbeddingPipelineService {

    /**
     * Embed (hoặc skip nếu hash không đổi) toàn bộ catalog xe có deleted_at IS NULL.
     * Trả về stats để log/monitor.
     */
    EmbedStats regenerateAll();

    /**
     * Phiên bản subset cho demo / dev — chỉ embed N xe đầu (theo car_id).
     * Hữu ích khi free tier Gemini bị siết RPM/RPD.
     */
    EmbedStats regenerateAll(int limit);

    /**
     * Embed 1 xe theo carId (gọi sau khi admin tạo/cập nhật xe).
     */
    void embedSingleCar(Integer carId);

    record EmbedStats(int total, int created, int updated, int skipped, int failed, long elapsedMs) {}
}
