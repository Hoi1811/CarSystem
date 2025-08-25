package web.car_system.Car_Service.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import web.car_system.Car_Service.utility.JwtTokenUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isTokenBypass(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null) {
            log.error("Missing token for request: {}", request.getServletPath());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        try {
            // Parse JWT và lấy claims
            Claims claims = jwtTokenUtil.extractAllClaims(token);
            String userId = claims.getSubject();
            String jti = claims.get("jti", String.class);

            // Kiểm tra token có bị thu hồi không
            String revokedKey = "revoked_token:" + jti;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(revokedKey))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                return;
            }

            // Lấy roles và permissions từ JWT nếu có
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) claims.get("permissions");

            // Nếu JWT không chứa roles/permissions, truy vấn Redis
            if (roles == null || permissions == null) {
                String redisKey = "user:auth:" + userId;
                roles = (List<String>) redisTemplate.opsForHash().get(redisKey, "roles");
                permissions = (List<String>) redisTemplate.opsForHash().get(redisKey, "permissions");

                if (roles == null || permissions == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User authorities not found");
                    return;
                }
            }

            // Kết hợp roles và permissions thành authorities
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));

            // Đưa vào SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Chuyển request đến controller
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
        }
    }

    private boolean isTokenBypass(HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = List.of(
                Pair.of("/paginated", "POST"),
                Pair.of("/manufacturers", "GET"),
                Pair.of("/car-types", "GET"),
                Pair.of("/car-segment-groups", "GET"),
                Pair.of("/car-segments", "GET"),
                Pair.of("/authorities", "GET"),
                Pair.of("/auth/login", "POST"),
                Pair.of("/auth/refresh", "POST"),
                Pair.of("/auth/logout", "POST"),
                Pair.of("/auth/register", "POST"),
                Pair.of("/oauth2", "GET"),
                Pair.of("chatbot", "POST"),
                Pair.of("/options", "GET"),
                Pair.of("/schema", "GET"),
                Pair.of("/suggestions", "GET"),
                Pair.of("/paginated", "POST"),
                Pair.of("/cars", "GET"),
                Pair.of("/images", "GET"),
                Pair.of("/related-cars", "POST"),
                Pair.of("/compare-cars", "POST"),
                Pair.of("/ai/suggest", "POST")
        );
        for (Pair<String, String> bypassToken : bypassTokens) {
            if (request.getServletPath().contains(bypassToken.getFirst())
                    && request.getMethod().equals(bypassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}