package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.user.AdminCreateUserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.ChangePasswordRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UpdateProfileRequestDTO;
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

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;
    private final Cloudinary cloudinary;

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
            userRepository.delete(user);
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
            user.setRoles(roles);
            userRepository.save(user);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Roles assigned to user successfully")
                    .build();
            // Xóa cache user + danh sách
            redisService.deleteFromCache(cacheKey);
            redisService.deleteKeysWithPrefix("users:all:");
            redisService.deleteKeysWithPrefix("userRoles:" + userId);
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

    // ===================== ADMIN-ONLY OPERATIONS =====================

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> adminCreateUser(AdminCreateUserRequestDTO request) {
        try {
            if (userRepository.findByUsername(request.username()).isPresent()) {
                return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Username đã tồn tại").build())
                        .build();
            }
            if (userRepository.findByEmail(request.email()).isPresent()) {
                return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Email đã tồn tại").build())
                        .build();
            }
            User user = new User();
            user.setUsername(request.username());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setEmail(request.email());
            user.setFullName(request.fullName());
            user.setEnabled(true);
            User savedUser = userRepository.save(user);

            // Gán roles — nếu không chỉ định thì dùng ROLE_USER mặc định
            List<String> rolesToAssign = (request.roles() != null && !request.roles().isEmpty())
                    ? request.roles()
                    : List.of("ROLE_USER");
            for (String roleName : rolesToAssign) {
                Role role = roleRepository.findByName(roleName).orElse(null);
                if (role != null) {
                    savedUser.getRoles().add(role);
                }
            }
            userRepository.save(savedUser);

            UserResponseDTO dto = userMapper.toDTO(savedUser);
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Tạo tài khoản thành công").build())
                    .data(dto)
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Tạo tài khoản thất bại: " + e.getMessage()).build())
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> adminResetPassword(Long userId, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            // Xóa cache authorities để buộc re-auth
            redisService.deleteFromCache("users:" + userId);
            redisTemplate.delete("user_authorities:" + userId);
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Đặt lại mật khẩu thành công").build())
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(e.getMessage()).build())
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> toggleUserStatus(Long userId, boolean isEnabled) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
            user.setEnabled(isEnabled);
            userRepository.save(user);
            redisService.deleteFromCache("users:" + userId);
            redisTemplate.delete("user_authorities:" + userId);
            String msg = isEnabled ? "Kích hoạt tài khoản thành công" : "Vô hiệu hóa tài khoản thành công";
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message(msg).build())
                    .build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(e.getMessage()).build())
                    .build();
        }
    }

    // ===================== SELF-SERVICE PROFILE OPERATIONS =====================

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> updateMyProfile(Long userId, UpdateProfileRequestDTO request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

            // Validate email unique (trừ chính user đó)
            if (request.email() != null && !request.email().equals(user.getEmail())) {
                Optional<User> existingByEmail = userRepository.findByEmail(request.email());
                if (existingByEmail.isPresent() && !existingByEmail.get().getUserId().equals(userId)) {
                    return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                            .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Email đã được sử dụng bởi tài khoản khác").build())
                            .build();
                }
            }

            userMapper.updateProfileEntity(user, request);
            User savedUser = userRepository.save(user);
            UserResponseDTO dto = userMapper.toDTO(savedUser);

            redisService.deleteFromCache("users:" + userId);
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Cập nhật thông tin cá nhân thành công").build())
                    .data(dto).build();
        } catch (NotFoundException e) {
            return GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(e.getMessage()).build()).build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, String> updateMyAvatar(Long userId, MultipartFile file) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

            // Validate file
            if (file == null || file.isEmpty()) {
                return GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("File ảnh không được để trống").build()).build();
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Chỉ chấp nhận file ảnh").build()).build();
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Kích thước ảnh tối đa 2MB").build()).build();
            }

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "user_avatars", "public_id", "user_" + userId));
            String imageUrl = (String) uploadResult.get("secure_url");

            user.setPicture(imageUrl);
            userRepository.save(user);
            redisService.deleteFromCache("users:" + userId);

            return GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Cập nhật ảnh đại diện thành công").build())
                    .data(imageUrl).build();
        } catch (Exception e) {
            return GlobalResponseDTO.<NoPaginatedMeta, String>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Cập nhật ảnh thất bại: " + e.getMessage()).build()).build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> changeMyPassword(Long userId, ChangePasswordRequestDTO request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

            // Chỉ user local mới có thể đổi mật khẩu
            if (!"local".equalsIgnoreCase(user.getProvider()) && user.getProvider() != null) {
                return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR)
                                .message("Tài khoản đăng nhập qua " + user.getProvider() + " không thể đổi mật khẩu").build()).build();
            }

            // Verify mật khẩu hiện tại
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Mật khẩu hiện tại không đúng").build()).build();
            }

            user.setPassword(passwordEncoder.encode(request.newPassword()));
            userRepository.save(user);
            redisService.deleteFromCache("users:" + userId);

            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Đổi mật khẩu thành công").build()).build();
        } catch (NotFoundException e) {
            return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                    .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(e.getMessage()).build()).build();
        }
    }
}