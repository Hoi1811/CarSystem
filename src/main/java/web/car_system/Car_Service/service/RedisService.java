package web.car_system.Car_Service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;

public interface RedisService {

    // Lưu dữ liệu vào cache
    <M, D> void saveToCache(String key, GlobalResponseDTO<M, D> data, long ttlInSeconds);

    // Lấy dữ liệu từ cache
    <M, D> GlobalResponseDTO<M, D> getFromCache(String key, TypeReference<GlobalResponseDTO<M, D>> type);

    // Xóa dữ liệu khỏi cache
    void deleteFromCache(String key);

    // Xóa toàn bộ cache (nếu cần)
    void clearAllCache();
}