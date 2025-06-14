package web.car_system.Car_Service.service;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerCreateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerUpdateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerResponseDTO;

public interface ManufacturerService {
    GlobalResponseDTO<?, ManufacturerResponseDTO> createManufacturer(ManufacturerCreateDTO createDTO) throws IOException;
    GlobalResponseDTO<PaginatedMeta, List<ManufacturerResponseDTO>> getAllManufacturers(Pageable pageable);
    GlobalResponseDTO<?, ManufacturerResponseDTO> updateManufacturer(Integer manufacturerId, ManufacturerUpdateDTO updateDTO) throws IOException;
    GlobalResponseDTO<?, Void> deleteManufacturer(Integer manufacturerId);

}
