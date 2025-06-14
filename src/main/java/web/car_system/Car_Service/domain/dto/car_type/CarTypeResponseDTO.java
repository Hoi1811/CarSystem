package web.car_system.Car_Service.domain.dto.car_type;

public record CarTypeResponseDTO(
        Integer typeId,
        String name,
        String description,
        String thumbnail
) {}