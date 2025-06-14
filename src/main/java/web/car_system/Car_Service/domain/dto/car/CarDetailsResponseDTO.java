package web.car_system.Car_Service.domain.dto.car;

import web.car_system.Car_Service.domain.dto.specification.SpecificationResponseDTO;
import web.car_system.Car_Service.domain.entity.Origin;

import java.math.BigDecimal;
import java.util.List;

public record CarDetailsResponseDTO(
        Integer carId,
        Integer manufacturerId,
        Integer segmentId,
        String name,
        String model,
        Integer year,
        BigDecimal price,
        String thumbnail,
        List<Integer> carTypeIds,
        Origin origin,
        List<SpecificationResponseDTO> specifications
) {
    public CarDetailsResponseDTO withThumbnail(String newThumbnail) {
        return new CarDetailsResponseDTO(carId, manufacturerId,segmentId,name,model,year,price,  newThumbnail, carTypeIds, origin, specifications);
}
}
