package web.car_system.Car_Service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestApiV1
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
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
        String provider = "";
        try {
            provider = new String(Base64.getUrlDecoder().decode(state));
            logger.info("Decoded provider from state: {}", provider);

            // Gọi service để xử lý code, lấy token, lấy user info, tạo/cập nhật user, tạo JWT
            GlobalResponseDTO<NoPaginatedMeta, Map<String, String>> authServiceResponse =
                    authService.handleOAuth2Callback(provider, code, true);

            // Nếu service xử lý thành công và trả về token của bạn
            if (authServiceResponse.meta().status() == Status.SUCCESS && authServiceResponse.data() != null) {
                Map<String, String> tokens = authServiceResponse.data();

                // Thêm accessToken và refreshToken của bạn vào HttpOnly cookies
                authService.addTokensToCookies(tokens,true, httpServletResponse);
                logger.info("OAuth2 login successful for provider {}, redirecting to frontend: {}", provider, frontendOAuthRedirectUri);

                // CHUYỂN HƯỚNG VỀ FRONTEND SAU KHI ĐẶT COOKIES THÀNH CÔNG
                httpServletResponse.sendRedirect(frontendOAuthRedirectUri); // Không có query params lỗi
            } else {
                // Nếu service xử lý có lỗi (ví dụ: không đổi được code lấy token, user không hợp lệ, ...)
                String errorMessage = authServiceResponse.meta().message() != null ? authServiceResponse.meta().message() : "Lỗi xử lý OAuth2 không xác định.";
                logger.error("OAuth2 callback processing error for provider {}: {}", provider, errorMessage);

                // CHUYỂN HƯỚNG VỀ FRONTEND VỚI THÔNG BÁO LỖI
                String errorRedirectUrl = frontendOAuthRedirectUri + "?error=true&message=" +
                        URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
                httpServletResponse.sendRedirect(errorRedirectUrl);
            }
        } catch (Exception e) {
            // Bắt các lỗi không mong muốn khác trong quá trình xử lý
            String safeProviderName = provider.isEmpty() ? "unknown provider" : provider;
            logger.error("Critical OAuth2 callback failed for provider '{}': {}", safeProviderName, e.getMessage(), e);
            String errorMessage = "Lỗi hệ thống trong quá trình đăng nhập OAuth2.";
            String errorRedirectUrl = frontendOAuthRedirectUri + "?error=true&message=" +
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
            httpServletResponse.sendRedirect(errorRedirectUrl);
        }
    }

    @PostMapping(Endpoint.V1.AUTH.REGISTER)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> register(
            @RequestBody RegisterRequestDTO request,
            HttpServletResponse response) {
        try {
            authService.registerLocalUser(request.username(), request.password(), request.email());
            authService.loginLocalUser(request.username(), request.password(),request.rememberMe(), response);

            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Register Success")
                            .build())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Register Error: " + e.getMessage())
                                    .build())
                            .build());

        }
    }

    @PostMapping(Endpoint.V1.AUTH.LOGIN)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> login(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        try {
            authService.loginLocalUser(request.username(), request.password(),request.rememberMe(), response);
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Login Success")
                            .build())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Login Error: " + e.getMessage())
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
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        try {
            authService.refreshToken(refreshToken, response);
            return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder()
                            .status(Status.SUCCESS)
                            .message("Register Success")
                            .build())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Register Error: " + e.getMessage())
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
            return ResponseEntity.badRequest().body(
                    GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                            .meta(NoPaginatedMeta.builder()
                                    .status(Status.ERROR)
                                    .message("Logout Error: " + e.getMessage())
                                    .build())
                            .build());

        }
    }
}