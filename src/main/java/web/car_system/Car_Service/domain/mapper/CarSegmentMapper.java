package web.car_system.Car_Service.domain.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentResponseDTO;
import web.car_system.Car_Service.domain.entity.CarSegment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CarSegmentGroupMapper.class)
public interface CarSegmentMapper {
    @Mapping(source = "segmentId", target = "segmentId")
    @Mapping(source = "group.id", target = "groupId")
    CarSegmentResponseDTO toResponseDTO(CarSegment segment);
}
