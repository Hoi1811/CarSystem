package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.domain.dto.user.UserAuthoritiesDTO;
import web.car_system.Car_Service.domain.dto.user.UserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserResponseDTO;
import web.car_system.Car_Service.domain.entity.Role;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.UserMapper;
import web.car_system.Car_Service.service.RoleService;
import web.car_system.Car_Service.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final UserMapper userMapper; // Inject mapper

    @GetMapping(Endpoint.V1.USER.ME) // Full path /api/v1/users/me
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                            .meta(NoPaginatedMeta.builder().status(Status.ERROR).message("Người dùng chưa được xác thực.").build())
                            .build());
        }

        Long userId = Long.parseLong(authentication.getName());
        User user = userService.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + userId));

        // Sử dụng mapper để chuyển đổi User entity sang UserResponseDTO
        UserResponseDTO userProfileDto = userMapper.toDTO(user);

        return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, UserResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy thông tin người dùng thành công.").build())
                .data(userProfileDto)
                .build());
    }

    @GetMapping(Endpoint.V1.USER.USER_AUTHORITIES) // Full path /api/v1/users/{userId}/authorities
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, UserAuthoritiesDTO>> getUserAuthorities(@PathVariable Long userId) {
        GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> userResponse = userService.getUserById(userId);
        if (userResponse.meta().status() == Status.ERROR) {
            return ResponseEntity.status(404).body(GlobalResponseDTO.<NoPaginatedMeta, UserAuthoritiesDTO>builder()
                    .meta(userResponse.meta())
                    .build());
        }
        UserAuthoritiesDTO response = new UserAuthoritiesDTO(
                roleService.getRolesByUserId(userId).data(),
                roleService.getPermissionsByUserId(userId).data()
        );
        NoPaginatedMeta meta = NoPaginatedMeta.builder()
                .status(Status.SUCCESS)
                .message("Authorities retrieved successfully")
                .build();
        return ResponseEntity.ok(GlobalResponseDTO.<NoPaginatedMeta, UserAuthoritiesDTO>builder()
                .meta(meta)
                .data(response)
                .build());
    }

    @PostMapping(Endpoint.V1.USER.USER) // Full path /api/v1/users
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO>> createUser(@RequestBody UserRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = userService.createUser(request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping(Endpoint.V1.USER.USER_ID) // Full path /api/v1/users/{userId}
    @PreAuthorize("hasAuthority('READ')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO>> getUserById(@PathVariable Long userId) {
        GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = userService.getUserById(userId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    @PutMapping(Endpoint.V1.USER.USER_ID) // Full path /api/v1/users/{userId}
    @PreAuthorize("hasAuthority('UPDATE')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequestDTO request) {
        GlobalResponseDTO<NoPaginatedMeta, UserResponseDTO> response = userService.updateUser(userId, request);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    @DeleteMapping(Endpoint.V1.USER.USER_ID) // Full path /api/v1/users/{userId}
    @PreAuthorize("hasAuthority('DELETE')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteUser(@PathVariable Long userId) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = userService.deleteUser(userId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(404).body(response);
    }

    @PostMapping(Endpoint.V1.USER.USER_ROLES) // Full path /api/v1/users/{userId}/roles
    @PreAuthorize("hasAuthority('UPDATE')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> addRole(
            @PathVariable Long userId,
            @RequestParam String roleName) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = userService.addRoleToUser(userId, roleName);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    @DeleteMapping(Endpoint.V1.USER.USER_ROLES) // Full path /api/v1/users/{userId}/roles
    @PreAuthorize("hasAuthority('UPDATE')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> removeRole(
            @PathVariable Long userId,
            @RequestParam String roleName) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = userService.removeRoleFromUser(userId, roleName);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping(Endpoint.V1.USER.USER_ROLES) // Full path /api/v1/users/{userId}/roles
    @PreAuthorize("hasAuthority('READ')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Set<Role>>> getUserRoles(@PathVariable Long userId) {
        GlobalResponseDTO<NoPaginatedMeta, Set<Role>> response = userService.getUserRoles(userId);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(response);
    }

    @PostMapping(Endpoint.V1.USER.USER_ROLES + "/bulk") // Full path /api/v1/users/{userId}/roles/bulk
    @PreAuthorize("hasAuthority('UPDATE')")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> assignRoles(
            @PathVariable Long userId,
            @RequestBody List<String> roleNames) {
        GlobalResponseDTO<NoPaginatedMeta, Void> response = userService.assignRolesToUser(userId, roleNames);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping(Endpoint.V1.USER.USER) // Full path /api/v1/users
    @PreAuthorize("hasAuthority('READ_ALL')")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") short pageSize) {
        GlobalResponseDTO<PaginatedMeta, List<UserResponseDTO>> response = userService.getAllUsers(pageIndex, pageSize);
        if (response.meta().status() == Status.SUCCESS) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(400).body(response);
    }
}