package web.car_system.Car_Service.domain.dto.manufacturer;

public record ManufacturerResponseDTO(
        Integer manufacturerId,
        String name,
        String thumbnail
) {
}
