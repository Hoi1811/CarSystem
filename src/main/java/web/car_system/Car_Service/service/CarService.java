package web.car_system.Car_Service.service;

import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.car.*;
import web.car_system.Car_Service.domain.dto.global.FilterCarPaginationRequestDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.projection.OptionProjection;

import java.io.IOException;
import java.util.List;

public interface CarService {

    GlobalResponseDTO<?, CarDetailsResponseDTO> createCar(AddCarRequestDTO carRequest);
    GlobalResponseDTO<?, CarDetailsResponseDTO> createCarV2(AddCarRequestDTO carRequest);
    GlobalResponseDTO<?, CarDetailsResponseDTO> createCarV3(AddCarRequestDTO carRequest);
    GlobalResponseDTO<?, CarDetailsResponseDTO> getCarById(Integer id);
    GlobalResponseDTO<?, List<CarDetailsResponseDTO>> getAllCars();
    GlobalResponseDTO<PaginatedMeta, List<CarResponseDTO>> getAllCarsPaginated(int page);
    GlobalResponseDTO<PaginatedMeta, List<CarResponseDTO>> getAllCarsPaginated(FilterCarPaginationRequestDTO filter);
    GlobalResponseDTO<?, CarDetailsResponseDTO> updateCar(Integer carId, UpdateCarRequestDTO carRequest, List<MultipartFile> newImages);
    GlobalResponseDTO<?, Void> deleteCar(Integer id);
    GlobalResponseDTO<NoPaginatedMeta, List<CarDetailsResponseDTO>> importCarsFromJsonFile(MultipartFile jsonFile) throws IOException;
    GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedCarsByName(FindRelatedCarsRequestDTO requestDTO);
    GlobalResponseDTO<NoPaginatedMeta, List<CarDetailsResponseDTO>> compareCars(CompareCarsRequestDTO compareCarsRequestDTO);
    GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedModelsByCarName(FindRelatedCarsRequestDTO requestDTO);
    GlobalResponseDTO<NoPaginatedMeta, List<OptionProjection>> findRelatedCarNamesByCarName(FindRelatedCarsRequestDTO requestDTO);
}
