-- V6: AI RAG — bảng lưu vector embedding của xe
-- Pipeline RAG (Retrieval-Augmented Generation):
--   1) buildCarText(car) → đoạn văn mô tả xe (JOIN car_attribute)
--   2) Gemini embedContent(text) → float[768]
--   3) Lưu vector + hash để skip re-embed khi xe không đổi
--
-- Lưu ý: project đang dùng spring.jpa.hibernate.ddl-auto=update,
-- Hibernate sẽ tự sinh bảng từ Entity CarEmbedding nếu chạy app.
-- File này giữ nguyên để có audit trail và để chạy thủ công trên prod.

CREATE TABLE IF NOT EXISTS car_embeddings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id              INT NOT NULL,
    embedding_json      LONGTEXT NOT NULL,
    source_text_hash    VARCHAR(64) NOT NULL,
    source_text         LONGTEXT NOT NULL,
    model_version       VARCHAR(50) NOT NULL,
    dimensions          INT NOT NULL,
    embedded_at         DATETIME NOT NULL,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME(6) NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',

    CONSTRAINT fk_car_embedding_car FOREIGN KEY (car_id) REFERENCES cars(car_id),
    CONSTRAINT uk_car_embedding_car_model UNIQUE (car_id, model_version),
    INDEX idx_car_embedding_car (car_id),
    INDEX idx_car_embedding_model (model_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
