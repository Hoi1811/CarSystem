package web.car_system.Car_Service.service;

public interface AuditRetentionService {

    /**
     * Xóa các revision cũ hơn cutoff (epoch millis) trong toàn bộ bảng *_aud cấu hình.
     * Trả về tổng số dòng đã xóa (cộng dồn từ tất cả bảng).
     */
    long purgeOlderThan(long cutoffEpochMillis);

    /**
     * Tiện ích: dùng retentionMonths từ cấu hình để tính cutoff và gọi {@link #purgeOlderThan(long)}.
     */
    long purgeUsingConfiguredRetention();
}
