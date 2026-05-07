package web.car_system.Car_Service.service.advisor;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/**
 * Retrieval layer của RAG — tìm top-K xe có embedding gần nhất với query vector.
 *
 * Cài đặt mặc định: cache toàn bộ embeddings vào RAM
 * (Map<carId, CarVecInfo>) để cosine chạy <5ms cho ~1000 xe.
 * Khi catalog vượt 100K xe, swap implementation này sang pgvector/Qdrant
 * mà KHÔNG cần đổi code RagAdvisorService — interface giữ nguyên.
 *
 * Hybrid retrieval: cache nhúng kèm `price` + `seats` để filter cứng (SQL-like)
 * trong cùng pass với cosine — cosine + filter chỉ cần 1 lần duyệt.
 */
public interface VectorSearchService {

    /**
     * (Re)load toàn bộ embeddings từ DB vào cache.
     * Gọi khi: app startup, sau khi scheduler regenerate xong, hoặc khi admin trigger.
     */
    void reloadCache();

    /**
     * Tìm top-K xe có cosine cao nhất so với queryVector. Đã filter min-score.
     */
    List<ScoredCar> findTopK(float[] queryVector, int k);

    /**
     * Phiên bản hybrid: chỉ xét những xe match `filter` predicate (vd price ≤ X, seats ≥ Y).
     * Dùng cho query có constraint cứng — tránh trường hợp cosine match yếu nhưng vượt budget.
     */
    List<ScoredCar> findTopK(float[] queryVector, int k, Predicate<CarVecInfo> filter);

    /**
     * Số embedding hiện đang trong cache (cho health check / admin endpoint).
     */
    int cacheSize();

    /**
     * Metadata kèm vector — cho phép filter mà không cần query DB.
     * `isElectric` = true cho pure EV (có pin, không có động cơ xăng/dầu).
     */
    record CarVecInfo(Integer carId, float[] vector, BigDecimal price, Integer seats, boolean isElectric) {}

    /**
     * Kết quả retrieval — pair (carId, cosine score).
     */
    record ScoredCar(Integer carId, double score) {}
}
