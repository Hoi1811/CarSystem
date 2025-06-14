package web.car_system.Car_Service.service;

import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupCreateDTO;

import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupResponseDTO;

import java.util.List;

public interface CarSegmentGroupService {
    GlobalResponseDTO<?, CarSegmentGroupResponseDTO> createGroup(CarSegmentGroupCreateDTO createDTO);
    GlobalResponseDTO<?, CarSegmentGroupResponseDTO> getGroupById(Integer id);
    GlobalResponseDTO<?, List<CarSegmentGroupResponseDTO>> getAllGroups();
    GlobalResponseDTO<?, CarSegmentGroupResponseDTO> updateGroup(Integer id, CarSegmentGroupUpdateDTO updateDTO);
    GlobalResponseDTO<?, Void> deleteGroup(Integer id);
    GlobalResponseDTO<PaginatedMeta, List<CarSegmentGroupResponseDTO>> getAllGroups(Pageable pageable);
}
