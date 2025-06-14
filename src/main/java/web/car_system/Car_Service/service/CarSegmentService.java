package web.car_system.Car_Service.service;

import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentBatchCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentResponseDTO;


import java.util.List;

public interface CarSegmentService {
    GlobalResponseDTO<?, CarSegmentResponseDTO> createSegment(CarSegmentCreateDTO createDTO);
    GlobalResponseDTO<?, CarSegmentResponseDTO> getSegmentById(Integer segmentId);
    GlobalResponseDTO<?, List<CarSegmentResponseDTO>> getAllSegments();
    GlobalResponseDTO<?, CarSegmentResponseDTO> updateSegment(Integer segmentId, CarSegmentUpdateDTO updateDTO);
    GlobalResponseDTO<?, Void> deleteSegment(Integer segmentId);
    GlobalResponseDTO<PaginatedMeta, List<CarSegmentResponseDTO>> getAllSegments(Pageable pageable);
    GlobalResponseDTO<?, List<CarSegmentResponseDTO>> getSegmentsByGroup(Integer groupId);
    // Thêm vào CarSegmentService
    GlobalResponseDTO<?, List<CarSegmentResponseDTO>> createBatchSegments(CarSegmentBatchCreateDTO batchCreateDTO);
}
