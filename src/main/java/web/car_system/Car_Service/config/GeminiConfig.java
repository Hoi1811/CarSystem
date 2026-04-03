package web.car_system.Car_Service.config;

import com.google.genai.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Singleton Gemini Client — tránh tạo 2 instance riêng lẻ trong @PostConstruct
 * gây race condition native resource khi startup.
 */
@Configuration
public class GeminiConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiConfig.class);

    @Bean
    public Client geminiClient(@Value("${google.gemini.api.key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_API_KEY")) {
            log.error("Google Gemini API key is not configured properly!");
            throw new IllegalArgumentException("Google Gemini API key is missing or invalid.");
        }
        Client client = Client.builder().apiKey(apiKey).build();
        log.info("Gemini Client initialized successfully (singleton).");
        return client;
    }
}
