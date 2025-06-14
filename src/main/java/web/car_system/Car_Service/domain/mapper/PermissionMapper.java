package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import web.car_system.Car_Service.domain.dto.permission.PermissionRequestDTO;
import web.car_system.Car_Service.domain.dto.permission.PermissionResponseDTO;
import web.car_system.Car_Service.domain.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toEntity(PermissionRequestDTO dto);
    PermissionResponseDTO toDTO(Permission permission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PermissionRequestDTO dto, @MappingTarget Permission entity);
}
