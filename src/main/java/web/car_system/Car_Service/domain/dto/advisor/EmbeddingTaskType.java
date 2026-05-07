package web.car_system.Car_Service.domain.dto.advisor;

/**
 * Loại tác vụ truyền cho Gemini Embedding API.
 * Tham khảo: https://ai.google.dev/gemini-api/docs/embeddings#task-type
 *
 * Dùng đúng task type giúp embedding "hợp gu" mục đích sử dụng:
 * - RETRIEVAL_DOCUMENT: text được index vào kho (mỗi xe trong catalog).
 * - RETRIEVAL_QUERY:    câu hỏi tìm kiếm của user.
 */
public enum EmbeddingTaskType {
    RETRIEVAL_DOCUMENT,
    RETRIEVAL_QUERY,
    SEMANTIC_SIMILARITY,
    CLASSIFICATION,
    CLUSTERING
}
