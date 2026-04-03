-- ============================================================
-- Migration: Enhance Car Comments - Multi-level reply + remove edit window
-- ============================================================

-- 1. Add reply_to_user fields for flat-threaded reply model
ALTER TABLE car_comments
    ADD COLUMN IF NOT EXISTS reply_to_user_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS reply_to_user_name VARCHAR(255) NULL;

-- 2. Add FK for reply_to_user_id
ALTER TABLE car_comments
    ADD CONSTRAINT fk_car_comment_reply_to_user FOREIGN KEY (reply_to_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
