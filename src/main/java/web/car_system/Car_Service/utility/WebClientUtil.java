package web.car_system.Car_Service.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Thay thế WebClient (WebFlux/Reactor Netty) bằng RestClient (blocking, dùng Tomcat thread).
 * Loại bỏ xung đột Netty vs Tomcat gây crash 0xC0000005.
 */
@Configuration
public class WebClientUtil {

    private static final Logger log = LoggerFactory.getLogger(WebClientUtil.class);

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
