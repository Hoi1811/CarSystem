package web.car_system.Car_Service.utility;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class WebClientUtil {
    @Bean
    public WebClient getWebClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(handleErrors())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // Log request details if needed
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(
                                "Error response: " + clientResponse.statusCode() + ", body: " + errorBody)));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

}
