package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;

import java.util.List;

public interface PersonalizationService {

    List<CarResponseDTO> getRecentViewedCars(Long userId, int limit);

    List<CarResponseDTO> getCarsByIds(List<Integer> ids, int limit);

    List<CarResponseDTO> getMaybeInterestedCars(Long userId, List<Integer> seedCarIds, int limit);
}
