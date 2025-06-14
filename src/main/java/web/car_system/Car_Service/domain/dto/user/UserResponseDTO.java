package web.car_system.Car_Service.domain.dto.user;


import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;

// Trả về thông tin user (ẩn sensitive data)
public record UserResponseDTO(
        Long userId,
        String email,
        String fullName,
        String provider,
        LocalDateTime createdAt,
        Set<RoleResponseDTO> roles  // Nested DTO
) {}
