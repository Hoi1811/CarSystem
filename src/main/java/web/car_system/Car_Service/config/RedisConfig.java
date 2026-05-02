package web.car_system.Car_Service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableCaching
@Configuration
public class RedisConfig {

    public static final String KEY_PREFIX = "cdss::v1::";

    public static final String CACHE_CAR_SEGMENTS = "carSegments";
    public static final String CACHE_CAR_SEGMENTS_BY_GROUP = "carSegmentsByGroup";
    public static final String CACHE_CAR_SEGMENT_GROUPS = "carSegmentGroups";
    public static final String CACHE_CAR_TYPES = "carTypes";
    public static final String CACHE_MANUFACTURERS = "manufacturers";
    public static final String CACHE_SPECIFICATIONS = "specifications";
    public static final String CACHE_FORM_SCHEMA = "formSchema";
    public static final String CACHE_ATTRIBUTES = "attributes";
    public static final String CACHE_REGIONAL_FEES = "regionalFees";
    public static final String CACHE_COMPARISON_RULES = "comparisonRules";
    public static final String CACHE_CAR_DETAILS = "carDetails";

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setPassword(redisPassword);
        return new LettuceConnectionFactory(config);
    }


    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(buildRedisObjectMapper());

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .computePrefixWith(name -> KEY_PREFIX + name + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        Duration day = Duration.ofDays(1);
        Duration sixHours = Duration.ofHours(6);
        Duration fifteenMin = Duration.ofMinutes(15);

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CACHE_CAR_SEGMENTS, defaults.entryTtl(day));
        perCache.put(CACHE_CAR_SEGMENTS_BY_GROUP, defaults.entryTtl(day));
        perCache.put(CACHE_CAR_SEGMENT_GROUPS, defaults.entryTtl(day));
        perCache.put(CACHE_CAR_TYPES, defaults.entryTtl(day));
        perCache.put(CACHE_MANUFACTURERS, defaults.entryTtl(day));
        perCache.put(CACHE_SPECIFICATIONS, defaults.entryTtl(sixHours));
        perCache.put(CACHE_FORM_SCHEMA, defaults.entryTtl(sixHours));
        perCache.put(CACHE_ATTRIBUTES, defaults.entryTtl(sixHours));
        perCache.put(CACHE_REGIONAL_FEES, defaults.entryTtl(day));
        perCache.put(CACHE_COMPARISON_RULES, defaults.entryTtl(day));
        perCache.put(CACHE_CAR_DETAILS, defaults.entryTtl(fifteenMin));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Sử dụng GenericJackson2JsonRedisSerializer với ObjectMapper đã cấu hình
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(buildRedisObjectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * ObjectMapper riêng cho Redis cache — KHÔNG expose thành `@Bean` để Spring Boot
     * không pick nó làm primary `ObjectMapper` cho Web. Nếu là bean public, Web sẽ
     * dùng nó để parse HTTP request body và đòi mọi JSON client gửi lên phải có
     * trường `@class` → vỡ toàn bộ API.
     *
     * Default typing ở đây là bắt buộc cho cache: các method `@Cacheable` trả
     * generic wrapper như `GlobalResponseDTO<?, T>` cần `@class` trong JSON Redis
     * mới deserialize đúng kiểu, không bị fallback `LinkedHashMap` → ClassCastException.
     *
     * Validator chỉ allow base `Object` — đủ permissive vì cache là dữ liệu nội bộ
     * do app tự ghi/đọc, không phải input từ client. Khi đổi config, FLUSH các key
     * `cdss::v1::*` cũ trong Redis vì format JSON cũ không có `@class`.
     */
    private ObjectMapper buildRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }
}