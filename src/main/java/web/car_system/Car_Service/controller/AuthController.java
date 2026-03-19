package web.car_system.Car_Service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.auth.LoginRequestDTO;
import web.car_system.Car_Service.domain.dto.auth.RegisterRequestDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.AuthService;
import web.car_system.Car_Service.service.FailedLoginAttemptService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestApiV1
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final FailedLoginAttemptService attemptService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${app.frontend.oauth_redirect_uri:http://localhost:4200/auth/oauth2/processing}")
    private String frontendOAuthRedirectUri;
    // OAuth2: Chuyển hướng đến Google/Facebook (không thay đổi)
    @GetMapping(Endpoint.V1.AUTH.AUTHORIZE)
    public void authorize(@RequestParam String provider, HttpServletResponse response) throws IOException {
        String redirectUrl = authService.getOAuth2AuthorizationUrl(provider);
        response.sendRedirect(redirectUrl);
    }

    // OAuth2 Callback: Xử lý token từ Google/Facebook
    @GetMapping(Endpoint.V1.AUTH.OAUTH2CALLBACK)
    public void oauth2Callback( // ĐỔI KIỂU TRẢ VỀ THÀNH void
                                @RequestParam String code,
                                @RequestParam String state,
                                HttpServletResponse httpServletResponse) throws IOException { // Đổi tên biến cho rõ ràng
        String providerHint = extractProviderHint(state);
        try {
            // Gọi service để validate state (CSRF), xử lý code, lấy token, tạo/cập nhật user, tạo JWT
            GlobalResponseDTO<NoPaginatedMeta, Map<String, String>> authServiceResponse =
                    authService.handleOAuth2Callback(code, state, true);

            // Nếu service xử lý thành công và trả về token của bạn
            if (authServiceResponse.meta().status() == Status.SUCCESS && authServiceResponse.data() != null) {
                Map<String, String> tokens = authServiceResponse.data();

                // Thêm accessToken và refreshToken của bạn vào HttpOnly cookies
                authService.addTokensToCookies(tokens, true, httpServletResponse);
                logger.info("OAuth2 login successful for provider {}, redirecting to frontend: {}", providerHint, frontendOAuthRedirectUri);

                // CHUYỂN HƯỚNG VỀ FRONTEND SAU KHI ĐẶT COOKIES THÀNH CÔNG
                httpServletResponse.sendRedirect(frontendOAuthRedirectUri);
            } else {
                // Nếu service xử lý có lỗi (ví dụ: state CSRF không hợp lệ, lỗi đổi code, ...)
                String errorMessage = authServiceResponse.meta().message() != null ? authServiceResponse.meta().message() : "Lỗi xử lý OAuth2 không xác định.";
                logger.error("OAuth2 callback processing error for provider {}: {}", providerHint, errorMessage);

                // CHUYỂN HƯỚNG VỀ FRONTEND VỚI THÔNG BÁO LỖI
                String errorRedirectUrl = frontendOAuthRedirectUri + "?error=true&message=" +
                        URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
                httpServletResponse.sendRedirect(errorRedirectUrl);
            }
        } catch (Exception e) {
            logger.error("Critical OAuth2 callback failed for provider '{}': {}", providerHint, e.getMessage(), e);
            String errorMessage = "Lỗi hệ thống trong quá trình đăng nhập OAuth2.";
            String errorRedirectUrl = frontendOAuthRedirectUri + "?error=true&message=" +
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
            httpServletResponse.sendRedirect(errorRedirectUrl);
        }
    }

    @PostMapping(Endpoint.V1.AUTH.REGISTER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletResponse response) {
        try {
            authService.registerLocalUser(request.username(), request.password(), request.email());
            // Auto-login removed - better UX to redirect to login page
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Đăng ký thành công. Vui lòng đăng nhập.")
                            .build())
                    .build());
        } catch (Exception e) {
            // Log detailed error for debugging, but return generic message to user
            logger.error("Registration failed for username: {}", request.username(), e);
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.")
                                    .build())
                            .build());
        }
    }

    @PostMapping(Endpoint.V1.AUTH.LOGIN)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        
        String clientIP = getClientIP(httpRequest);
        String loginKey = clientIP + ":" + request.username();
        
        // Check if IP/username combo is blocked
        if (attemptService.isBlocked(loginKey)) {
            long remainingMinutes = attemptService.getRemainingBlockTime(loginKey);
            logger.warn("Blocked login attempt from: {}", clientIP);
            return ResponseEntity.status(429).body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message(String.format("Quá nhiều lần thử. Vui lòng đợi %d phút.", remainingMinutes))
                                    .build())
                            .build());
        }
        
        try {
            authService.loginLocalUser(request.username(), request.password(), request.rememberMe(), response);
            
            // Reset failed attempts counter on successful login
            attemptService.loginSucceeded(loginKey);
            
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Đăng nhập thành công")
                            .build())
                    .build());
        } catch (Exception e) {
            // Increment failed attempts counter
            attemptService.loginFailed(loginKey);
            
            // Log failed attempt with details, but return generic message
            logger.warn("Login failed for username: {} from IP: {}", request.username(), clientIP);
            return ResponseEntity.status(401).body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Tên đăng nhập hoặc mật khẩu không đúng")
                                    .build())
                            .build());
        }
    }

    @GetMapping(Endpoint.V1.AUTH.VALIDATE_ADMIN)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> validateAdmin() {
        // Kiểm tra quyền admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Unauthorized")
                                    .build())
                            .build());
        }
        return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Validate Admin Success")
                        .build())
                .build());
    }

    @PostMapping(Endpoint.V1.AUTH.REFRESH_TOKEN)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalArgumentException("Refresh token không hợp lệ");
            }
            authService.refreshToken(refreshToken, response);
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Làm mới token thành công")
                            .build())
                    .build());
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.status(401).body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Làm mới token thất bại")
                                    .build())
                            .build());
        }
    }

    @PostMapping(Endpoint.V1.AUTH.LOGOUT)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> logout(
            HttpServletResponse response, HttpServletRequest request) {
        try {
            authService.logout(response, request);
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Logout Success")
                            .build())
                    .build());
        } catch (Exception e) {
            logger.error("Logout failed", e);
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Đăng xuất thất bại. Vui lòng thử lại.")
                                    .build())
                            .build());
        }
    }

    /**
     * Best-effort decode provider name from OAuth2 state for logging purposes only.
     * Security validation of the full state is performed inside AuthService.
     */
    private String extractProviderHint(String state) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 2);
            return parts.length >= 1 ? parts[0] : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Lấy IP address của client dùng cho brute-force tracking.
     * Dùng getRemoteAddr() để tránh bị giả mạo qua header X-Forwarded-For.
     * Nếu chạy sau một trusted reverse proxy, cấu hình proxy để ghi đè RemoteAddr ở server level.
     */
    private String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}