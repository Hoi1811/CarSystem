package web.car_system.Car_Service.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import web.car_system.Car_Service.config.security.filter.JwtFilter;
import web.car_system.Car_Service.constant.Endpoint;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(Endpoint.V1.AUTH.LOGIN, Endpoint.V1.AUTH.AUTHORIZE,Endpoint.V1.AUTH.OAUTH2CALLBACK , Endpoint.V1.AUTH.REGISTER, Endpoint.V1.AUTH.REFRESH_TOKEN, Endpoint.V1.AUTH.LOGOUT).permitAll()
                        .requestMatchers(Endpoint.V1.USER.ME).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(Endpoint.V1.OPTIONS.OPTIONS_BY_SOURCE_NAME).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.SPECIFICATIONS_SCHEMA).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.CAR_ID_SUGGESTIONS).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.COMPARE_CARS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cars/*/images").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/cars/*/images").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(GET, "/api/v1/specifications").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(POST, "api/v1/cars/paginated").permitAll()
                        .requestMatchers(POST, "api/v1/cars/related-cars").permitAll()
                        .requestMatchers(POST, "api/v1/cars/related-models").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, "api/v1/cars/related-car-names").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, "/api/v1/specifications").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(POST,"/api/v1/permissions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(POST,"/api/v1/roles/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(POST,"/api/v1/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(GET,"/api/v1/manufacturers").permitAll()
                        .requestMatchers(GET,"/api/v1/manufacturers/**").permitAll()
                        .requestMatchers(GET, "/api/v1/car-types").permitAll()
                        .requestMatchers(GET, "/api/v1/car-segment-groups").permitAll()
                        .requestMatchers(GET, "/api/v1/car-segments").permitAll()
                        .requestMatchers(GET, "/api/v1/cars/**").permitAll()
                        .requestMatchers(POST, "/api/v1/cars/v2").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(GET, "api/v1/users/*/authorities").permitAll()
                        .requestMatchers("/api/v1/permissions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/roles/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/users/**").hasAuthority("ROLE_ADMIN")

                        .requestMatchers(POST, Endpoint.V1.CHATBOT.CHAT).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Cho phép tất cả yêu cầu OPTIONS

                        .requestMatchers(HttpMethod.GET, Endpoint.V1.AUTH.VALIDATE_ADMIN).authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.error("Authentication error for {}: {}", request.getServletPath(), authException.getMessage());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            log.error("Access denied for {}: {}", request.getServletPath(), ex.getMessage());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(ALLOWED_HTTP_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setMaxAge((long) (24 * 60 * 60));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    private final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "http://localhost:1234",
            "http://localhost:4200",
            "https://just-a-normal-dev.id.vn",
            "https://be.just-a-normal-dev.id.vn",
            "http://10.2.0.78:4200",
            "http://10.1.8.15:4200"
    );
    private final List<String> ALLOWED_HTTP_METHODS = List.of(
            GET.toString(),
            POST.toString(),
            PUT.toString(),
            PATCH.toString(),
            DELETE.toString(),
            OPTIONS.toString()
    );
    private final List<String> ALLOWED_HEADERS      = List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.ACCEPT_LANGUAGE,
            HttpHeaders.CONTENT_TYPE
    );
    private final List<String> EXPOSED_HEADERS      = List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.ACCEPT_LANGUAGE
    );
}