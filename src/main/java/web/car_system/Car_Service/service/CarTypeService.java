package web.car_system.Car_Service.service;

import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeCreateDTO;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeResponseDTO;

import java.io.IOException;
import java.util.List;

public interface CarTypeService {
    GlobalResponseDTO<?, CarTypeResponseDTO> createCarType(CarTypeCreateDTO createDTO) throws IOException;
    GlobalResponseDTO<?, CarTypeResponseDTO> getCarTypeById(Integer typeId);
    GlobalResponseDTO<?, List<CarTypeResponseDTO>> getAllCarTypes();
    GlobalResponseDTO<PaginatedMeta, List<CarTypeResponseDTO>> getAllCarTypes(Pageable pageable);
    GlobalResponseDTO<?, CarTypeResponseDTO> updateCarType(Integer typeId, CarTypeUpdateDTO updateDTO) throws IOException;
    GlobalResponseDTO<?, Void> deleteCarType(Integer typeId);
}
