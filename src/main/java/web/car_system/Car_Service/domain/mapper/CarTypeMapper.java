package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeResponseDTO;
import web.car_system.Car_Service.domain.entity.CarType;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarTypeMapper {
    CarTypeResponseDTO toResponseDTO(CarType carType);
}