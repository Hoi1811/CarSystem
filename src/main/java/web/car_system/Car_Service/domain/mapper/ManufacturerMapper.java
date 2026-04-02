package web.car_system.Car_Service.domain.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerCreateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerResponseDTO;
import web.car_system.Car_Service.domain.entity.Manufacturer;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ManufacturerMapper {
    @Mapping(target = "manufacturerId", ignore = true)
    @Mapping(target = "thumbnail", ignore = true)
    Manufacturer toEntity(ManufacturerCreateDTO requestDTO);
    ManufacturerResponseDTO toResponseDTO(Manufacturer manufacturer);
}