package web.car_system.Car_Service.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.oauth2.OAuth2TokenResponseDTO;
import web.car_system.Car_Service.domain.dto.user.OAuth2UserInfo;
import web.car_system.Car_Service.domain.dto.user.OAuth2UserInfoFacebook;
import web.car_system.Car_Service.domain.dto.user.OAuth2UserInfoGoogle;
import web.car_system.Car_Service.domain.dto.user.UserAuthoritiesDTO;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.AuthService;
import web.car_system.Car_Service.service.RoleService;
import web.car_system.Car_Service.service.UserService;
import web.car_system.Car_Service.utility.JwtTokenUtil;
import web.car_system.Car_Service.utility.UserInformationUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final WebClient webClient;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger log = Logger.getLogger(AuthServiceImpl.class.getName());

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;
    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String googleAuthorizationUri;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String googleTokenUri;
    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;
    @Value("${spring.security.oauth2.client.provider.facebook.authorization-uri}")
    private String facebookAuthorizationUri;
    @Value("${spring.security.oauth2.client.provider.facebook.token-uri}")
    private String facebookTokenUri;
    @Value("${spring.security.oauth2.client.provider.facebook.user-info-uri}")
    private String facebookUserInfoUri;
    @Value("${jwt.ttl-in-seconds}")
    private long defaultTtlInSeconds;

    @Override
    public String getOAuth2AuthorizationUrl(String provider) {
        String state = Base64.getUrlEncoder().encodeToString(provider.getBytes());
        return switch (provider.toLowerCase()) {
            case "google" -> String.format(googleAuthorizationUri +
                            "?client_id=%s&redirect_uri=%s&response_type=code&scope=profile email&state=%s",
                    googleClientId, redirectUri, state);
            case "facebook" -> String.format(facebookAuthorizationUri +
                            "?client_id=%s&redirect_uri=%s&scope=public_profile,email&response_type=code&state=%s",
                    facebookClientId, redirectUri, state);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, Map<String, String>> handleOAuth2Callback(String provider, String code, boolean rememberMeByDefault) {
        try {
            OAuth2TokenResponseDTO tokenResponse = exchangeCodeForToken(provider, code).block();
            if (tokenResponse == null) throw new RuntimeException("Failed to exchange code for token");
            OAuth2UserInfo userInfo = verifyOAuth2Token(provider, tokenResponse.access_token()).block();
            if (userInfo == null) throw new RuntimeException("Invalid OAuth2 token");
            User user = createOAuth2User(userInfo.id(), userInfo.name(), provider, userInfo.email(), userInfo.picture());
            Map<String, String> tokens = jwtTokenUtil.generateTokenPair(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("OAuth2 login successful")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Map<String, String>>builder()
                    .meta(meta)
                    .data(tokens)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Map<String, String>>builder()
                    .meta(meta)
                    .build();
        }
    }

    private Mono<OAuth2TokenResponseDTO> exchangeCodeForToken(String provider, String code) {
        String tokenUri = switch (provider.toLowerCase()) {
            case "google" -> googleTokenUri;
            case "facebook" -> facebookTokenUri;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
        String clientId = provider.equals("google") ? googleClientId : facebookClientId;
        String clientSecret = provider.equals("google") ? googleClientSecret : facebookClientSecret;

        return webClient.post()
                .uri(tokenUri)
                .bodyValue(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .bodyToMono(OAuth2TokenResponseDTO.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Token exchange failed: " + e.getMessage())));
    }

    private Mono<OAuth2UserInfo> verifyOAuth2Token(String provider, String accessToken) {
        String userInfoUri = switch (provider.toLowerCase()) {
            case "google" -> googleUserInfoUri;
            case "facebook" -> facebookUserInfoUri;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
        Class<? extends OAuth2UserInfo> userInfoClass = provider.equals("google")
                ? OAuth2UserInfoGoogle.class
                : OAuth2UserInfoFacebook.class;
        return webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(userInfoClass)
                .cast(OAuth2UserInfo.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Token verification failed: " + e.getMessage())));
    }

    @Transactional
    public User createOAuth2User(String externalId, String fullName, String provider, String email, String picture) {
        User user = userRepository.findByExternalIdAndProvider(externalId, provider)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setExternalId(externalId);
                    newUser.setProvider(provider);
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setUsername(externalId);
                    newUser.setPicture(picture);
                    return newUser;
                });
        User savedUser = userRepository.save(user);
        userService.addRoleToUser(savedUser.getUserId(), "ROLE_USER");
        return savedUser;
    }

    @Transactional
    @Override
    public GlobalResponseDTO<NoPaginatedMeta, User> registerLocalUser(String username, String password, String email) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            User savedUser = userRepository.save(user);
            userService.addRoleToUser(savedUser.getUserId(), "ROLE_USER");
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User registered successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, User>builder()
                    .meta(meta)
                    .data(savedUser)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, User>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public void loginLocalUser(String username, String password, boolean rememberMe, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("User account is deleted");
        }

        Map<String, String> tokens = jwtTokenUtil.generateTokenPair(user);
        addTokensToCookies(tokens, rememberMe, response);
    }

    @Override
    public void refreshToken(String refreshToken, HttpServletResponse response) {
        Integer userId = (Integer) redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        if (userId == null || redisTemplate.hasKey("blacklist:refresh_token:" + refreshToken)) {
            throw new RuntimeException("Invalid refresh token, please try again");
        }

        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenUtil.generateAccessToken(user);

        ResponseCookie cookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public void logout(HttpServletResponse response, HttpServletRequest request) {
        Pair<String, String> tokenPair = UserInformationUtil.resolveToken(request);
        String accessToken = tokenPair.getFirst();
        String refreshToken = tokenPair.getSecond();

        jwtTokenUtil.revokeToken(accessToken);
        jwtTokenUtil.revokeRefreshToken(refreshToken);

        String userId = (String) redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        if (userId != null) {
            redisTemplate.delete("refresh_token:" + userId);
            redisTemplate.delete("user_authorities:" + userId);
        }

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    @Override
    public void addTokensToCookies(Map<String, String> tokens,boolean rememberMe, HttpServletResponse response) {
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // AccessToken luôn có thời hạn ngắn (ví dụ: 15 phút)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true) // Nên là true trong production (HTTPS)
                .path("/")
                .maxAge(15 * 60) // 15 phút
                .sameSite("None")
                .build();

        long refreshTokenMaxAge;
        if (rememberMe) {
            refreshTokenMaxAge = 7 * 24 * 60 * 60; // Ví dụ: 7 ngày cho "Remember Me"
        } else {
            // Nếu không "Remember Me", refreshToken sẽ là session cookie (bị xóa khi đóng trình duyệt)
            // Hoặc bạn có thể đặt một thời hạn ngắn hơn, ví dụ bằng accessToken, hoặc 0 để xóa ngay khi hết phiên.
            // Để làm nó thành session cookie, không set Max-Age.
            // Tuy nhiên, một số trình duyệt có thể vẫn giữ session cookie một lúc.
            // Đặt Max-Age = -1 hoặc không set gì cả thường tạo session cookie.
            // Để rõ ràng hơn và đảm bảo nó bị xóa khi accessToken hết hạn (nếu không remember me),
            // bạn có thể đặt maxAge của refreshToken bằng maxAge của accessToken.
            // Hoặc, nếu bạn muốn nó bị xóa ngay khi đóng trình duyệt khi không "remember me",
            // bạn không cần đặt maxAge (hoặc đặt là -1 theo một số cách triển khai, nhưng không set là chuẩn hơn).
            refreshTokenMaxAge = -1; // Sẽ làm cho nó thành session cookie (bị xóa khi đóng trình duyệt)
            // Hoặc bạn có thể đặt một thời hạn ngắn hơn accessToken nếu muốn nó hết hạn trước
        }

        ResponseCookie.ResponseCookieBuilder refreshCookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // Nên là true trong production
                .path("/")
                .sameSite("None");

        if (refreshTokenMaxAge >= 0) { // Chỉ đặt Max-Age nếu nó không phải là session cookie mặc định
            refreshCookieBuilder.maxAge(refreshTokenMaxAge);
        }
        // Nếu refreshTokenMaxAge là -1 (hoặc bạn bỏ qua việc set maxAge), nó sẽ là session cookie

        ResponseCookie refreshCookie = refreshCookieBuilder.build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
        log.info("accessCookie: " + accessCookie.toString());
}
}