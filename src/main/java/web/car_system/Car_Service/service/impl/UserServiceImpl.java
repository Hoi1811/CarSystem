package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.user.UserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserResponseDTO;
import web.car_system.Car_Service.domain.entity.Role;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.UserMapper;
import web.car_system.Car_Service.exception.NotFoundException;
import web.car_system.Car_Service.repository.RoleRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.RedisService;
import web.car_system.Car_Service.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> createUser(UserRequestDTO request) {
        try {
            User user = userMapper.toEntity(request);
            User savedUser = userRepository.save(user);
            UserResponseDTO responseDTO = userMapper.toDTO(savedUser);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User created successfully")
                    .build();
            GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
            // Lưu vào cache sau khi tạo
            redisService.saveToCache("users:" + savedUser.getUserId(), response, 3600); // TTL 1 giờ
            return response;
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to create user: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> getUserById(Long userId) {
        // Kiểm tra cache trước
        String cacheKey = "users:" + userId;
        GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> cachedResponse = redisService.getFromCache(cacheKey, new TypeReference<GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO>>() {});
        if (cachedResponse != null) {
            return cachedResponse;
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            UserResponseDTO responseDTO = userMapper.toDTO(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User retrieved successfully")
                    .build();
            GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
            // Lưu vào cache
            redisService.saveToCache(cacheKey, response, 3600); // TTL 1 giờ
            return response;
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> updateUser(Long userId, UserRequestDTO request) {
        String cacheKey = "users:" + userId;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userMapper.updateEntity(user, request);
            UserResponseDTO updatedUser = userMapper.toDTO(userRepository.save(user));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User updated successfully")
                    .build();
            GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .data(updatedUser)
                    .build();
            // Cập nhật cache
            redisService.saveToCache(cacheKey, response, 3600); // TTL 1 giờ
            return response;
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, Void> deleteUser(Long userId) {
        String cacheKey = "users:" + userId;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            user.setDeleted(true);
            user.setWhenDeleted(LocalDateTime.now());
            userRepository.save(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User deleted successfully")
                    .build();
            // Xóa cache
            redisService.deleteFromCache(cacheKey);
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, Void> addRoleToUser(Long userId, String roleName) {
        String cacheKey = "users:" + userId;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
            if (user.getRoles().contains(role)) {
                throw new RuntimeException("User already has this role");
            }
            user.getRoles().add(role);
            userRepository.save(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role added to user successfully")
                    .build();
            // Xóa cache để cập nhật lại sau
            redisService.deleteFromCache(cacheKey);
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, Void> removeRoleFromUser(Long userId, String roleName) {
        String cacheKey = "users:" + userId;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
            if (!user.getRoles().remove(role)) {
                throw new RuntimeException("User does not have this role");
            }
            userRepository.save(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role removed from user successfully")
                    .build();
            // Xóa cache
            redisService.deleteFromCache(cacheKey);
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, Set<Role>> getUserRoles(Long userId) {
        String cacheKey = "userRoles:" + userId;
        GlobalResponseDTO<NoPaginatedMeta, Set<Role>> cachedResponse = redisService.getFromCache(cacheKey, new TypeReference<GlobalResponseDTO<NoPaginatedMeta, Set<Role>>>() {});
        if (cachedResponse != null) {
            return cachedResponse;
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            Set<Role> roles = user.getRoles();
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("User roles retrieved successfully")
                    .build();
            GlobalResponseDTO<NoPaginatedMeta, Set<Role>> response = GlobalResponseDTO.<NoPaginatedMeta, Set<Role>>builder()
                    .meta(meta)
                    .data(roles)
                    .build();
            // Lưu vào cache
            redisService.saveToCache(cacheKey, response, 3600); // TTL 1 giờ
            return response;
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Set<Role>>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> assignRolesToUser(Long userId, List<String> roleNames) {
        String cacheKey = "users:" + userId;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            Set<Role> roles = roleNames.stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseThrow(() -> new NotFoundException("Role not found: " + name)))
                    .collect(Collectors.toSet());
            user.getRoles().addAll(roles);
            userRepository.save(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Roles assigned to user successfully")
                    .build();
            // Xóa cache
            redisService.deleteFromCache(cacheKey);
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>> getAllUsers(int pageIndex, short pageSize) {
        String cacheKey = "users:all:" + pageIndex + ":" + pageSize;
        GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>> cachedResponse = redisService.getFromCache(cacheKey, new TypeReference<GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>>>() {});
        if (cachedResponse != null) {
            return cachedResponse;
        }

        try {
            Pageable pageable = PageRequest.of(pageIndex, pageSize);
            Page<User> userPage = userRepository.findAll(pageable);
            List<UserResponseDTO> users = userPage.getContent().stream()
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());

            Pagination pagination = Pagination.builder()
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalItems(userPage.getTotalElements())
                    .totalPages(userPage.getTotalPages())
                    .build();

            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Users retrieved successfully")
                    .pagination(pagination)
                    .build();

            GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>> response = GlobalResponseDTO.<PaginatedMeta, List<UserResponseDTO>>builder()
                    .meta(meta)
                    .data(users)
                    .build();
            // Lưu vào cache
            redisService.saveToCache(cacheKey, response, 3600); // TTL 1 giờ
            return response;
        } catch (Exception e) {
            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to retrieve users: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<PaginatedMeta, List<UserResponseDTO>>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }
}