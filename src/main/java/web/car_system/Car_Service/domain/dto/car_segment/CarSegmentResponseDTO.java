package web.car_system.Car_Service.domain.dto.car_segment;


public record CarSegmentResponseDTO(
        Integer segmentId,
        String name,
        String description,
        Integer groupId
) {}
