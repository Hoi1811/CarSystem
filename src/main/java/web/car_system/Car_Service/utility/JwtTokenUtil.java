package web.car_system.Car_Service.utility;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.user.UserAuthoritiesDTO;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.RoleService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtil {

    @Value("${jwt.private-key}")
    private String privateKeyPath;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    @Value("${jwt.ttl-in-seconds}")
    private Long defaultTtlInSeconds;

    @Value("${jwt.refresh-ttl-in-seconds}")
    private Long defaultRefreshTtlInSeconds;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RoleService roleService;

    @PostConstruct
    public void init() throws Exception {
        // --- SỬA LẠI CÁCH ĐỌC PRIVATE KEY ---
        try (InputStream privateKeyStream = new ClassPathResource(privateKeyPath).getInputStream()) {
            String privateKeyContent = StreamUtils.copyToString(privateKeyStream, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // dùng replaceAll("\\s", "") để xóa mọi khoảng trắng, kể cả xuống dòng

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.privateKey = kf.generatePrivate(privateSpec);
        }

        // --- SỬA LẠI CÁCH ĐỌC PUBLIC KEY ---
        try (InputStream publicKeyStream = new ClassPathResource(publicKeyPath).getInputStream()) {
            String publicKeyContent = StreamUtils.copyToString(publicKeyStream, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", ""); // dùng replaceAll("\\s", "") để xóa mọi khoảng trắng, kể cả xuống dòng

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA"); // Bạn có thể tái sử dụng KeyFactory đã tạo ở trên
            this.publicKey = kf.generatePublic(publicSpec);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error extracting claim: {}", e.getMessage());
            throw new RuntimeException("Error processing JWT token");
        }
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> claims.get("permissions", List.class));
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractExternalId(String token) {
        return extractClaim(token, claims -> claims.get("externalId", String.class));
    }

    public String extractAuthType(String token) {
        return extractClaim(token, claims -> claims.get("authType", String.class));
    }

    public String extractProvider(String token) {
        return extractClaim(token, claims -> claims.get("provider", String.class));
    }

    private void storeUserAuthoritiesInRedis(String userId, UserAuthoritiesDTO authorities, Long ttlInSeconds) {
        String redisKey = "user_authorities:" + userId;
        Map<String, Object> authData = new HashMap<>();
        authData.put("roles", authorities.roles());
        authData.put("permissions", authorities.permissions());
        redisTemplate.opsForHash().putAll(redisKey, authData);
        redisTemplate.expire(redisKey, ttlInSeconds, TimeUnit.SECONDS);
    }

    public Map<String, String> generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // Lấy authorities và lưu vào Redis
        UserAuthoritiesDTO authorities = getUserAuthorities(user.getUserId());
        if (authorities != null) {
            storeUserAuthoritiesInRedis(user.getUserId().toString(), authorities, defaultTtlInSeconds);
        } else {
            log.warn("No authorities found for userId: {}", user.getUserId());
        }

        // Lưu refresh token vào Redis
        String refreshRedisKey = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().set(refreshRedisKey, user.getUserId(), defaultRefreshTtlInSeconds, TimeUnit.SECONDS);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        UserAuthoritiesDTO authorities = getUserAuthorities(user.getUserId());
        if (authorities == null || authorities.roles() == null || authorities.permissions() == null) {
            log.warn("No authorities found for userId: {}", user.getUserId());
            claims.put("roles", List.of("ROLE_USER"));
            claims.put("permissions", List.of("READ"));
        } else {
            storeUserAuthoritiesInRedis(user.getUserId().toString(), authorities, defaultTtlInSeconds);
            claims.put("roles", authorities.roles());
            claims.put("permissions", authorities.permissions());
        }
        String subject = user.getUserId().toString();
        return buildToken(claims, subject, defaultTtlInSeconds);
    }

    private String generateRefreshToken(User user) {
        return UUID.randomUUID().toString() + user.getUserId();
    }

    public String refreshAccessToken(String refreshToken) {
        String redisKey = "refresh_token:" + refreshToken;
        String userId = (String) redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateAccessToken(user);
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            String redisKey = "refresh_token:" + refreshToken;
            redisTemplate.delete(redisKey);
            log.info("Refresh token {} revoked", refreshToken);
        } else {
            log.warn("Refresh token is null or empty, skipping Redis delete operation");
        }
    }

    private String buildToken(Map<String, Object> claims, String subject, Long ttlInSeconds) {
        String jti = UUID.randomUUID().toString();
        claims.put("jti", jti);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlInSeconds * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isValidToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String jti = claims.get("jti", String.class);
            if (redisTemplate.opsForValue().get("revoked_token:" + jti) != null) {
                log.warn("Token {} has been revoked", jti);
                return false;
            }
            if (isTokenExpired(token)) {
                log.error("Token {} has expired", jti);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public void revokeToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String jti = claims.get("jti", String.class);
            Long ttl = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                redisTemplate.opsForValue().set("revoked_token:" + jti, "revoked", ttl, TimeUnit.SECONDS);
                log.info("Token {} revoked successfully", jti);
            }
        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
    }

    private String getSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private UserAuthoritiesDTO getUserAuthorities(Long userId) {
        GlobalResponseDTO<NoPaginatedMeta, List<String>> rolesResponse = roleService.getRolesByUserId(userId);
        GlobalResponseDTO<NoPaginatedMeta, List<String>> permissionsResponse = roleService.getPermissionsByUserId(userId);
        if (rolesResponse.meta().status() == Status.SUCCESS && permissionsResponse.meta().status() == Status.SUCCESS) {
            return new UserAuthoritiesDTO(rolesResponse.data(), permissionsResponse.data());
        }
        return null;
    }
}