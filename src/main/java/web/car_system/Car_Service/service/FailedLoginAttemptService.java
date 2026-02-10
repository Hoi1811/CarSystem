package web.car_system.Car_Service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service để track và block failed login attempts
 * Prevents brute force attacks bằng cách block IP/username sau N lần thử sai
 */
@Service
@Slf4j
public class FailedLoginAttemptService {

    private final Map<String, AtomicInteger> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockCache = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5; // Max 5 failed attempts
    private static final int BLOCK_DURATION_MINUTES = 15; // Block for 15 minutes

    /**
     * Ghi nhận 1 lần login failed
     * @param key IP address hoặc username
     */
    public void loginFailed(String key) {
        int attempts = attemptsCache.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        log.warn("Login failed for key: {}. Attempt count: {}", key, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            LocalDateTime blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES);
            blockCache.put(key, blockedUntil);
            log.error("KEY BLOCKED: {} until {}", key, blockedUntil);
        }
    }

    /**
     * Reset counter khi login thành công
     * @param key IP address hoặc username
     */
    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockCache.remove(key);
        log.info("Login succeeded for key: {}. Counter reset.", key);
    }

    /**
     * Kiểm tra xem key có bị block không
     * @param key IP address hoặc username
     * @return true nếu đang bị block
     */
    public boolean isBlocked(String key) {
        LocalDateTime blockedUntil = blockCache.get(key);
        if (blockedUntil == null) {
            return false;
        }

        // Check if block duration has expired
        if (LocalDateTime.now().isAfter(blockedUntil)) {
            // Unblock
            blockCache.remove(key);
            attemptsCache.remove(key);
            log.info("Block expired for key: {}. Unblocked.", key);
            return false;
        }

        log.warn("Access denied for blocked key: {}. Blocked until {}", key, blockedUntil);
        return true;
    }

    /**
     * Lấy số lần attempt hiện tại
     * @param key IP address hoặc username
     * @return số lần attempt
     */
    public int getAttemptCount(String key) {
        AtomicInteger count = attemptsCache.get(key);
        return count == null ? 0 : count.get();
    }

    /**
     * Lấy thời gian block còn lại (in minutes)
     * @param key IP address hoặc username
     * @return số phút còn lại, hoặc 0 nếu không bị block
     */
    public long getRemainingBlockTime(String key) {
        LocalDateTime blockedUntil = blockCache.get(key);
        if (blockedUntil == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(blockedUntil)) {
            return 0;
        }

        return java.time.Duration.between(now, blockedUntil).toMinutes();
    }
}
