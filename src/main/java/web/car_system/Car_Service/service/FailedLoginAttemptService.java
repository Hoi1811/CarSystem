package web.car_system.Car_Service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service để track và block failed login attempts.
 * Dùng Redis để đảm bảo hoạt động đúng trong môi trường multi-instance và không mất dữ liệu khi restart.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FailedLoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;
    private static final String ATTEMPT_KEY_PREFIX = "failed_login_attempts:";
    private static final String BLOCK_KEY_PREFIX = "login_blocked:";

    /**
     * Ghi nhận 1 lần login failed.
     */
    public void loginFailed(String key) {
        String attemptKey = ATTEMPT_KEY_PREFIX + key;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            // Đặt TTL cho counter — tự động xóa sau khoảng thời gian block + buffer
            redisTemplate.expire(attemptKey, BLOCK_DURATION_MINUTES + 5L, TimeUnit.MINUTES);
        }
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(BLOCK_KEY_PREFIX + key, "1", BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.error("KEY BLOCKED: {} for {} minutes", key, BLOCK_DURATION_MINUTES);
        }
        log.warn("Login failed for key: {}. Attempt count: {}", key, attempts);
    }

    /**
     * Reset counter khi login thành công.
     */
    public void loginSucceeded(String key) {
        redisTemplate.delete(ATTEMPT_KEY_PREFIX + key);
        redisTemplate.delete(BLOCK_KEY_PREFIX + key);
        log.info("Login succeeded for key: {}. Counter reset.", key);
    }

    /**
     * Kiểm tra xem key có bị block không.
     */
    public boolean isBlocked(String key) {
        Boolean exists = redisTemplate.hasKey(BLOCK_KEY_PREFIX + key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Lấy số lần attempt hiện tại.
     */
    public int getAttemptCount(String key) {
        Object val = redisTemplate.opsForValue().get(ATTEMPT_KEY_PREFIX + key);
        return val == null ? 0 : Integer.parseInt(val.toString());
    }

    /**
     * Lấy thời gian block còn lại (phút).
     */
    public long getRemainingBlockTime(String key) {
        Long ttl = redisTemplate.getExpire(BLOCK_KEY_PREFIX + key, TimeUnit.MINUTES);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }
}
