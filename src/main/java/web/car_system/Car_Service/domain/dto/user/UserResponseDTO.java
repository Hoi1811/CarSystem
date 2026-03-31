package web.car_system.Car_Service.domain.dto.user;


import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;

// Trả về thông tin user (ẩn sensitive data)
public record UserResponseDTO(
        Long userId,
        String username,
        String email,
        String fullName,
        String provider,
        boolean isEnabled,
        LocalDateTime createdAt,
        Set<RoleResponseDTO> roles,  // Nested DTO
        // Showroom info (chi nhánh mà user thuộc về, null nếu là System Admin)
        Long showroomId,
        String showroomName,
        String showroomCode
) {}
