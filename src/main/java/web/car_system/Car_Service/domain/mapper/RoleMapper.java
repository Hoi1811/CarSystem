package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.*;
import web.car_system.Car_Service.domain.dto.role.RoleRequestDTO;
import web.car_system.Car_Service.domain.dto.role.RoleResponseDTO;
import web.car_system.Car_Service.domain.entity.Role;

@Mapper(
        componentModel = "spring",
        uses = {PermissionMapper.class}, // Sử dụng PermissionMapper để map nested Permission
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoleMapper {

    // --------------------- RequestDTO → Entity ---------------------
    @Mapping(target = "roleId", ignore = true) // Bỏ qua ID khi tạo mới
    @Mapping(target = "permissions", ignore = true) // Xử lý riêng trong Service
    Role toEntity(RoleRequestDTO dto);

    // --------------------- Entity → ResponseDTO ---------------------
    @Mapping(source = "permissions", target = "permissions") // Tự động map qua PermissionMapper
    RoleResponseDTO toDTO(Role entity);

    // --------------------- Cập nhật Entity từ DTO ---------------------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RoleRequestDTO dto, @MappingTarget Role entity);

    // --------------------- Custom Mapping Logic (nếu cần) ---------------------
    @AfterMapping
    default void afterMapping(RoleRequestDTO dto, @MappingTarget Role entity) {
        // Ví dụ: Chuẩn hóa tên role thành chữ hoa
        if (dto.name() != null) {
            entity.setName(dto.name().toUpperCase());
        }
    }
}