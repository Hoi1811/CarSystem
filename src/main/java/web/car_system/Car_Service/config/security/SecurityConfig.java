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
                        //==> Các endpoint công khai, không cần xác thực
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                Endpoint.V1.AUTH.LOGIN,
                                Endpoint.V1.AUTH.AUTHORIZE,
                                Endpoint.V1.AUTH.OAUTH2CALLBACK,
                                Endpoint.V1.AUTH.REGISTER,
                                Endpoint.V1.AUTH.REFRESH_TOKEN,
                                Endpoint.V1.AUTH.LOGOUT
                        ).permitAll()
                        .requestMatchers(Endpoint.V1.OPTIONS.OPTIONS_BY_SOURCE_NAME).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.SPECIFICATIONS_SCHEMA).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.CAR_ID_SUGGESTIONS).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.COMPARE_CARS).permitAll()
                        .requestMatchers(Endpoint.V1.CAR.CAR_PAGINATED).permitAll() // Sửa từ POST thành cho mọi method nếu cần
                        .requestMatchers(Endpoint.V1.CAR.FIND_RELATED_CARS_BY_NAME).permitAll()
                        .requestMatchers(Endpoint.V1.CHATBOT.CHAT).permitAll()
                        .requestMatchers(
                                Endpoint.V1.CAR.MANUFACTURER,
                                Endpoint.V1.CAR.MANUFACTURER + "/**",
                                Endpoint.V1.CAR.CAR_TYPE,
                                Endpoint.V1.CAR.CAR_SEGMENT_GROUP,
                                Endpoint.V1.CAR.CAR_SEGMENT
                        ).permitAll()
                        .requestMatchers(GET, Endpoint.V1.CAR.CAR + "/**").permitAll() // Cho phép GET toàn bộ thông tin xe
                        .requestMatchers(GET, Endpoint.V1.CAR.CAR_ID_IMAGES).permitAll()
                        .requestMatchers(GET, Endpoint.V1.USER.USER_AUTHORITIES).permitAll()

                        //==> Các endpoint yêu cầu vai trò USER hoặc ADMIN
                        .requestMatchers(Endpoint.V1.USER.ME).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, Endpoint.V1.CAR.CAR_ID_IMAGES).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, Endpoint.V1.CAR.FIND_RELATED_MODELS_BY_NAME).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, Endpoint.V1.CAR.FIND_RELATED_CAR_NAMES_BY_NAME).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(POST, Endpoint.V1.CAR.CAR_V2).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        //==> Các endpoint yêu cầu vai trò ADMIN
                        .requestMatchers(Endpoint.V1.CAR.SPECIFICATIONS).hasAuthority("ROLE_ADMIN") // Bao gồm cả GET và POST
                        .requestMatchers(Endpoint.V1.PERMISSION.PERMISSION + "/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(Endpoint.V1.ROLE.ROLE + "/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(Endpoint.V1.USER.USER + "/**").hasAuthority("ROLE_ADMIN")
                        // THÊM MỚI: Yêu cầu quyền ADMIN cho tất cả API về ATTRIBUTE
                        .requestMatchers(Endpoint.V1.ATTRIBUTE.ATTRIBUTE_PREFIX + "/**").hasAuthority("ROLE_ADMIN")

                        //
                        .requestMatchers(Endpoint.V1.COMPARISON_RULE.GET_ALL + "/**").hasAuthority("ROLE_ADMIN")

                        //==> Các endpoint yêu cầu xác thực (đã đăng nhập)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Luôn cho phép yêu cầu OPTIONS
                        .requestMatchers(GET, Endpoint.V1.AUTH.VALIDATE_ADMIN).authenticated()

                        //==> Tất cả các yêu cầu còn lại đều cần xác thực
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