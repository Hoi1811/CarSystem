package web.car_system.Car_Service.service.advisor;

import web.car_system.Car_Service.domain.dto.advisor.EmbeddingTaskType;

/**
 * Sinh vector embedding từ Gemini API.
 *
 * Thiết kế tách 2 method theo task type vì Gemini cần thông tin này
 * để optimize representation cho từng mục đích — giúp cosine khớp tốt hơn:
 *  - embedDocument(): cho text MÔ TẢ XE (lưu vào kho)
 *  - embedQuery():    cho CÂU HỎI người dùng (lúc retrieval)
 */
public interface EmbeddingService {

    /**
     * Embed text mô tả xe để index vào kho.
     * @return float[dimensions] — không null, không rỗng
     */
    float[] embedDocument(String text);

    /**
     * Embed câu hỏi/keyword của user dùng để retrieve.
     * @return float[dimensions]
     */
    float[] embedQuery(String text);

    /**
     * Method tổng quát cho test/debug — gọi với task type tùy ý.
     */
    float[] embed(String text, EmbeddingTaskType taskType);
}
