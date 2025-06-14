package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.*;
import web.car_system.Car_Service.domain.dto.user.UserCreateRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserRequestDTO;
import web.car_system.Car_Service.domain.dto.user.UserResponseDTO;
import web.car_system.Car_Service.domain.entity.User;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    // Map RequestDTO -> Entity (khi tạo mới)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roles", ignore = true) // Sẽ xử lý riêng
    User toEntity(UserRequestDTO dto);

    @Mapping(source = "userId", target = "userId")
    User toUser(UserCreateRequestDTO userCreateRequestDTO);

    // Map Entity -> ResponseDTO
    @Mapping(source = "roles", target = "roles")
    UserResponseDTO toDTO(User user);

    // Cập nhật Entity từ RequestDTO (không thay đổi ID và roles)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget User entity, UserRequestDTO dto);
}
