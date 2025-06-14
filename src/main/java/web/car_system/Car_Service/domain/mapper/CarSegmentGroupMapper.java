package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupResponseDTO;
import web.car_system.Car_Service.domain.entity.CarSegmentGroup;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarSegmentGroupMapper {
    CarSegmentGroupResponseDTO toResponseDTO(CarSegmentGroup group);
}