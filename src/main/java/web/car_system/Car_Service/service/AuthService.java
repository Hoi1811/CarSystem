package web.car_system.Car_Service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.entity.User;


import java.util.Map;

public interface AuthService {
    String getOAuth2AuthorizationUrl(String provider);
    GlobalResponseDTO<NoPaginatedMeta, Map<String, String>> handleOAuth2Callback(String provider, String code, boolean rememberMeByDefault);
    GlobalResponseDTO<NoPaginatedMeta, User> registerLocalUser(String username, String password, String email);
    void loginLocalUser(String username, String password, boolean rememberMe,  HttpServletResponse response);
    void refreshToken(String refreshToken,  HttpServletResponse response);
    void addTokensToCookies(Map<String, String> tokens, boolean rememberMe, HttpServletResponse response);
    void logout(HttpServletResponse response, HttpServletRequest request);

}
