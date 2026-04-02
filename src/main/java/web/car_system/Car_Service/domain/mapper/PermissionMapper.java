package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import web.car_system.Car_Service.domain.dto.permission.PermissionRequestDTO;
import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;
import web.car_system.Car_Service.domain.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "permissionId", ignore = true)
    Permission toEntity(PermissionRequestDTO dto);
    PermissionResponseDTO toDTO(Permission permission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "permissionId", ignore = true)
    void updateEntity(PermissionRequestDTO dto, @MappingTarget Permission entity);
}
