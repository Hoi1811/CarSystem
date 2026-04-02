package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.*;
import web.car_system.Car_Service.domain.dto.user.UpdateProfileRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserCreateRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserResponseDTO;
import web.car_system.Car_Service.domain.entity.User;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    // Map RequestDTO -> Entity (khi tạo mới)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roles", ignore = true) // Sẽ xử lý riêng
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "showroom", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(UserRequestDTO dto);

    @Mapping(source = "userId", target = "userId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "showroom", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    User toUser(UserCreateRequestDTO userCreateRequestDTO);

    // Map Entity -> ResponseDTO
    @Mapping(source = "roles", target = "roles")
    @Mapping(source = "showroom.id", target = "showroomId")
    @Mapping(source = "showroom.name", target = "showroomName")
    @Mapping(source = "showroom.code", target = "showroomCode")
    @Mapping(source = "enabled", target = "isEnabled")
    UserResponseDTO toDTO(User user);

    // Cập nhật Entity từ RequestDTO (không thay đổi ID và roles)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "showroom", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateEntity(@MappingTarget User entity, UserRequestDTO dto);

    // Cập nhật Entity từ UpdateProfileRequestDTO (self-update profile)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "showroom", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateProfileEntity(@MappingTarget User entity, UpdateProfileRequestDTO dto);
}
