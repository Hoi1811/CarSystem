package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.service.RedisService;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <M, D> void saveToCache(String key, GlobalResponseDTO<M, D> data, long ttlInSeconds) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, ttlInSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize data to cache: " + e.getMessage(), e);
        }
    }

    @Override
    public <M, D> GlobalResponseDTO<M, D> getFromCache(String key, TypeReference<GlobalResponseDTO<M, D>> typeReference) {
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize data from cache: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFromCache(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void clearAllCache() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}