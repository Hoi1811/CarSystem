package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.role.RoleRequestDTO;
import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;
import web.car_system.Car_Service.domain.entity.Permission;
import web.car_system.Car_Service.domain.entity.Role;
import web.car_system.Car_Service.domain.mapper.RoleMapper;
import web.car_system.Car_Service.exception.NotFoundException;
import web.car_system.Car_Service.repository.PermissionRepository;
import web.car_system.Car_Service.repository.RoleRepository;
import web.car_system.Car_Service.service.RoleService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> createRole(RoleRequestDTO request) {
        try {
            Role role = roleMapper.toEntity(request);
            Role savedRole = roleRepository.save(role);
            RoleResponseDTO responseDTO = roleMapper.toDTO(savedRole);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role created successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to create role: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> getRoleById(Long roleId) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            RoleResponseDTO responseDTO = roleMapper.toDTO(role);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role retrieved successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<PaginatedMeta, List<RoleResponseDTO>> getAllRoles(String keyword, int pageIndex, short pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageIndex, pageSize);
            Page<Role> rolePage = roleRepository.findByNameContaining(keyword, pageable);
            List<RoleResponseDTO> roles = rolePage.getContent().stream()
                    .map(roleMapper::toDTO)
                    .toList();

            Pagination pagination = Pagination.builder()
                    .keyword(keyword)
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalItems(rolePage.getTotalElements())
                    .totalPages(rolePage.getTotalPages())
                    .build();

            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Roles retrieved successfully")
                    .pagination(pagination)
                    .build();

            return GlobalResponseDTO.<PaginatedMeta, List<RoleResponseDTO>>builder()
                    .meta(meta)
                    .data(roles)
                    .build();
        } catch (Exception e) {
            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to retrieve roles: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<PaginatedMeta, List<RoleResponseDTO>>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> updateRole(Long roleId, RoleRequestDTO request) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            roleMapper.updateEntity(request, role);
            Role updatedRole = roleRepository.save(role);
            RoleResponseDTO responseDTO = roleMapper.toDTO(updatedRole);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role updated successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> deleteRole(Long roleId) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            roleRepository.delete(role);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Role deleted successfully")
                    .build();
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
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> addPermissionToRole(Long roleId, String permissionName) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new NotFoundException("Permission not found with name: " + permissionName));
            if (role.getPermissions().contains(permission)) {
                throw new RuntimeException("Permission already assigned to role");
            }
            role.getPermissions().add(permission);
            Role updatedRole = roleRepository.save(role);
            RoleResponseDTO responseDTO = roleMapper.toDTO(updatedRole);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission added to role successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, RoleResponseDTO> removePermissionFromRole(Long roleId, String permissionName) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new NotFoundException("Permission not found with name: " + permissionName));
            if (!role.getPermissions().remove(permission)) {
                throw new RuntimeException("Permission not found in role");
            }
            Role updatedRole = roleRepository.save(role);
            RoleResponseDTO responseDTO = roleMapper.toDTO(updatedRole);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission removed from role successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, RoleResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<NoPaginatedMeta, List<String>> getRolesByUserId(Long userId) {
        try {
            List<String> roles = roleRepository.findRoleNamesByUserId(userId);
            if (roles.isEmpty()) {
                roles = List.of("ROLE_USER");
            }
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Roles retrieved successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, List<String>>builder()
                    .meta(meta)
                    .data(roles)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to retrieve roles: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, List<String>>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<NoPaginatedMeta, List<String>> getPermissionsByUserId(Long userId) {
        try {
            List<String> permissions = roleRepository.findPermissionNamesByUserId(userId);
            if (permissions.isEmpty()) {
                permissions = List.of("READ");
            }
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permissions retrieved successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, List<String>>builder()
                    .meta(meta)
                    .data(permissions)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to retrieve permissions: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, List<String>>builder()
                    .meta(meta)
                    .build();
        }
    }
}