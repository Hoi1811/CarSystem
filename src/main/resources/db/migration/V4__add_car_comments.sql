-- ============================================================
-- Migration: Feature A - Bình Luận Xe (Car Comments)
-- Created: 2026-04-03
-- ============================================================

-- 1. Bảng car_comments
CREATE TABLE IF NOT EXISTS car_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id INT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    rating INT NULL CHECK (rating >= 1 AND rating <= 5),
    comment_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    entity_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',

    CONSTRAINT fk_car_comment_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE,
    CONSTRAINT fk_car_comment_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_car_comment_parent FOREIGN KEY (parent_id) REFERENCES car_comments(id) ON DELETE SET NULL,

    INDEX idx_car_comments_car_id (car_id),
    INDEX idx_car_comments_user_id (user_id),
    INDEX idx_car_comments_parent_id (parent_id),
    INDEX idx_car_comments_status (comment_status),
    INDEX idx_car_comments_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng comment_likes
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES car_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_comment_like UNIQUE (comment_id, user_id),

    INDEX idx_comment_likes_comment (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bổ sung cột aggregate vào bảng cars
ALTER TABLE cars
    ADD COLUMN IF NOT EXISTS average_comment_rating DOUBLE NULL,
    ADD COLUMN IF NOT EXISTS total_comments INT NOT NULL DEFAULT 0;
