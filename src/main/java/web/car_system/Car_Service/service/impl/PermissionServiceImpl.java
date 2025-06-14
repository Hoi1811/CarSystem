package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.permission.PermissionRequestDTO;
import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;
import web.car_system.Car_Service.domain.entity.Permission;
import web.car_system.Car_Service.domain.mapper.PermissionMapper;
import web.car_system.Car_Service.exception.NotFoundException;
import web.car_system.Car_Service.repository.PermissionRepository;
import web.car_system.Car_Service.service.PermissionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> createPermission(PermissionRequestDTO request) {
        try {
            Permission permission = permissionMapper.toEntity(request);
            Permission savedPermission = permissionRepository.save(permission);
            PermissionResponseDTO responseDTO = permissionMapper.toDTO(savedPermission);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission created successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (Exception e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to create permission: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> getPermissionById(Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NotFoundException("Permission not found with id: " + permissionId));
            PermissionResponseDTO responseDTO = permissionMapper.toDTO(permission);
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission retrieved successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponseDTO<PaginatedMeta, List<PermissionResponseDTO>> getAllPermissions(String keyword, int pageIndex, short pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageIndex, pageSize);
            Page<Permission> permissionPage = permissionRepository.findByNameContaining(keyword, pageable);
            List<PermissionResponseDTO> permissions = permissionPage.getContent().stream()
                    .map(permissionMapper::toDTO)
                    .toList();

            Pagination pagination = Pagination.builder()
                    .keyword(keyword)
                    .pageIndex(pageIndex)
                    .pageSize(pageSize)
                    .totalItems(permissionPage.getTotalElements())
                    .totalPages(permissionPage.getTotalPages())
                    .build();

            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permissions retrieved successfully")
                    .pagination(pagination)
                    .build();

            return GlobalResponseDTO.<PaginatedMeta, List<PermissionResponseDTO>>builder()
                    .meta(meta)
                    .data(permissions)
                    .build();
        } catch (Exception e) {
            PaginatedMeta meta = PaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message("Failed to retrieve permissions: " + e.getMessage())
                    .build();
            return GlobalResponseDTO.<PaginatedMeta, List<PermissionResponseDTO>>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, PermissionResponseDTO> updatePermission(Long permissionId, PermissionRequestDTO request) {
        try {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NotFoundException("Permission not found with id: " + permissionId));
            permissionMapper.updateEntity(request, permission);
            Permission updatedPermission = permissionRepository.save(permission);
            PermissionResponseDTO responseDTO = permissionMapper.toDTO(updatedPermission);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission updated successfully")
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .data(responseDTO)
                    .build();
        } catch (NotFoundException e) {
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.ERROR)
                    .message(e.getMessage())
                    .build();
            return GlobalResponseDTO.<NoPaginatedMeta, PermissionResponseDTO>builder()
                    .meta(meta)
                    .build();
        }
    }

    @Override
    @Transactional
    public GlobalResponseDTO<NoPaginatedMeta, Void> deletePermission(Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new NotFoundException("Permission not found with id: " + permissionId));
            permissionRepository.delete(permission);
            redisTemplate.delete(redisTemplate.keys("user_authorities:*"));
            NoPaginatedMeta meta = NoPaginatedMeta.builder()
                    .status(Status.SUCCESS)
                    .message("Permission deleted successfully")
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
}