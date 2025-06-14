package web.car_system.Car_Service.utility;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.util.Pair;


public final class UserInformationUtil {
    private UserInformationUtil() {
        // Prevent instantiation
    }
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent"); // e.g., "Mozilla/5.0 (Windows NT 10.0...)"
    }
    public static Pair<String, String> resolveToken(HttpServletRequest request) {
        String accessToken = "";
        String refreshToken = "";

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }
        return Pair.of(accessToken, refreshToken);
    }


}

