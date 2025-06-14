package web.car_system.Car_Service.service;



import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.user.UserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserResponseDTO;
import web.car_system.Car_Service.domain.entity.Role;
import web.car_system.Car_Service.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> createUser(UserRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> getUserById(Long userId);
    GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> updateUser(Long userId, UserRequestDTO request);
    GlobalResponseDTO<NoPaginatedMeta, Void> deleteUser(Long userId);

    // Quản lý Roles
    GlobalResponseDTO<NoPaginatedMeta, Void> addRoleToUser(Long userId, String roleName);
    GlobalResponseDTO<NoPaginatedMeta, Void> removeRoleFromUser(Long userId, String roleName);
    GlobalResponseDTO<NoPaginatedMeta, Set<Role>> getUserRoles(Long userId);
    GlobalResponseDTO<NoPaginatedMeta, Void> assignRolesToUser(Long userId, List<String> roleNames);

    // Thêm phương thức phân trang để dùng PaginatedMeta
    GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>> getAllUsers(int pageIndex, short pageSize);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);
}
