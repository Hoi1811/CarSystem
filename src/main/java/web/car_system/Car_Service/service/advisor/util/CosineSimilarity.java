package web.car_system.Car_Service.service.advisor.util;

/**
 * Cosine similarity giữa 2 vector — đo "góc" giữa chúng, không quan tâm độ dài.
 * Trả về [-1, 1]. Càng gần 1 = càng giống nghĩa.
 *
 * Lý do dùng cosine thay Euclidean:
 *  - Embedding của Gemini KHÔNG được L2-normalize sẵn → Euclidean nhạy với độ dài.
 *  - Cosine bỏ qua độ dài, chỉ so sánh hướng → ổn định hơn cho text.
 */
public final class CosineSimilarity {

    private CosineSimilarity() {}

    public static double compute(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vector dimension mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0d, normA = 0d, normB = 0d;
        for (int i = 0; i < a.length; i++) {
            dot   += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0d || normB == 0d) return 0d;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
