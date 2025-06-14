package web.car_system.Car_Service.domain.dto.car;

import lombok.Builder;
import web.car_system.Car_Service.domain.entity.Origin;

import java.math.BigDecimal;
import java.util.List;
@Builder
public record CarResponseDTO(
        Integer carId,
        Integer manufacturerId,
        Integer segmentId,
        String name,
        String model,
        Integer year,
        BigDecimal price,
        String thumbnail,
        List<Integer> carTypeIds,
        Origin origin
) {
    public CarResponseDTO withThumbnail(String newThumbnail) {
        return new CarResponseDTO(carId, manufacturerId,segmentId,name,model,year,price,  newThumbnail, carTypeIds, origin);
    }
}
