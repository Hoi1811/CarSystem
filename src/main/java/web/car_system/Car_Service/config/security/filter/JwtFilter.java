package web.car_system.Car_Service.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
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

        // 1. Cố gắng lấy token từ request
        String token = resolveToken(request);

        // 2. Nếu KHÔNG có token:
        // Cho request đi tiếp. Nếu URL này cần bảo mật, SecurityConfig sẽ chặn lại và trả về 403.
        // Nếu URL này là public (permitAll), SecurityConfig sẽ cho qua.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Nếu CÓ token: Thực hiện xác thực
        try {
            // Parse JWT và lấy claims
            Claims claims = jwtTokenUtil.extractAllClaims(token);
            String userId = claims.getSubject();
            String jti = claims.get("jti", String.class);

            // Kiểm tra token có bị thu hồi (Logout/Revoked) không trong Redis
            String revokedKey = "revoked_token:" + jti;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(revokedKey))) {
                // Token đã bị thu hồi -> Trả lỗi ngay lập tức
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                return;
            }

            // Lấy roles và permissions từ JWT
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) claims.get("permissions");

            // Nếu JWT không chứa roles/permissions (trường hợp dùng stateful hoặc token cũ), fallback sang Redis
            if (roles == null || permissions == null) {
                String redisKey = "user:auth:" + userId;
                roles = (List<String>) redisTemplate.opsForHash().get(redisKey, "roles");
                permissions = (List<String>) redisTemplate.opsForHash().get(redisKey, "permissions");

                if (roles == null || permissions == null) {
                    // Không tìm thấy quyền hạn -> coi như không hợp lệ
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

            // Thiết lập Authentication vào SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // Có thể set thêm details nếu cần: auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Nếu token sai format, hết hạn, hoặc key không đúng...
            log.error("Cannot set user authentication: {}", e.getMessage());

            // Xóa context để đảm bảo an toàn
            SecurityContextHolder.clearContext();

            // Trả về lỗi 401 Unauthorized để Client biết token không hợp lệ
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
            return;
        }

        // 4. Cho request đi tiếp (với SecurityContext đã được set nếu token hợp lệ)
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // Fallback: Lấy từ Cookie (nếu hệ thống của bạn hỗ trợ cả cookie)
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