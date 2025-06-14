package web.car_system.Car_Service.domain.dto.user;

import web.car_system.Car_Service.domain.dto.global.Pagination;

import java.util.List;

// Trả về danh sách user + phân trang
public record UserListResponseDTO(
        List<UserResponseDTO> users,
        Pagination pagination
) {}