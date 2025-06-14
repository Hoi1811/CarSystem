package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeCreateDTO;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_type.CarTypeResponseDTO;
import web.car_system.Car_Service.domain.entity.CarType;
import web.car_system.Car_Service.domain.mapper.CarTypeMapper;
import web.car_system.Car_Service.repository.CarTypeRepository;
import web.car_system.Car_Service.service.CarTypeService;
import web.car_system.Car_Service.service.ImageService;


import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarTypeServiceImpl implements CarTypeService {
    private final CarTypeRepository carTypeRepository;
    private final CarTypeMapper carTypeMapper;
    private final ImageService imageUploadService;

    @Override
    public GlobalResponseDTO<?, CarTypeResponseDTO> createCarType(CarTypeCreateDTO createDTO) throws IOException {
        // Upload thumbnail
        String thumbnailUrl = imageUploadService.uploadCarTypeThumbnail(createDTO.thumbnailFile());

        // Check duplicate name
        if (carTypeRepository.existsByName(createDTO.name())) {
            throw new RuntimeException("Tên loại xe đã tồn tại");
        }

        // Create car type
        CarType carType = CarType.builder()
                .name(createDTO.name())
                .description(createDTO.description())
                .thumbnail(thumbnailUrl)
                .build();

        CarType savedCarType = carTypeRepository.save(carType);

        return GlobalResponseDTO.<NoPaginatedMeta, CarTypeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Tạo loại xe thành công")
                        .build())
                .data(carTypeMapper.toResponseDTO(savedCarType))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, CarTypeResponseDTO> getCarTypeById(Integer typeId) {
        CarType carType = carTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));

        return GlobalResponseDTO.<NoPaginatedMeta, CarTypeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy thông tin loại xe thành công")
                        .build())
                .data(carTypeMapper.toResponseDTO(carType))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, List<CarTypeResponseDTO>> getAllCarTypes() {
        List<CarTypeResponseDTO> carTypes = carTypeRepository.findAll().stream()
                .map(carTypeMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarTypeResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách loại xe thành công")
                        .build())
                .data(carTypes)
                .build();
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarTypeResponseDTO>> getAllCarTypes(Pageable pageable) {
        Page<CarType> page = carTypeRepository.findAll(pageable);

        Pagination pagination = Pagination.builder()
                .pageIndex(page.getNumber())
                .pageSize((short) page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        List<CarTypeResponseDTO> carTypes = page.getContent().stream()
                .map(carTypeMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<PaginatedMeta, List<CarTypeResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách loại xe thành công")
                        .pagination(pagination)
                        .build())
                .data(carTypes)
                .build();
    }

    @Override
    public GlobalResponseDTO<?, CarTypeResponseDTO> updateCarType(Integer typeId, CarTypeUpdateDTO updateDTO) throws IOException {
        CarType carType = carTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));

        // Update thumbnail if new file provided
        if (updateDTO.thumbnailFile() != null && !updateDTO.thumbnailFile().isEmpty()) {
            String newThumbnailUrl = imageUploadService.uploadCarTypeThumbnail(updateDTO.thumbnailFile());
            carType.setThumbnail(newThumbnailUrl);
        }

        // Update other fields
        carType.setName(updateDTO.name());
        carType.setDescription(updateDTO.description());

        CarType updatedCarType = carTypeRepository.save(carType);

        return GlobalResponseDTO.<NoPaginatedMeta, CarTypeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Cập nhật loại xe thành công")
                        .build())
                .data(carTypeMapper.toResponseDTO(updatedCarType))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, Void> deleteCarType(Integer typeId) {
        carTypeRepository.deleteById(typeId);

        return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Xóa loại xe thành công")
                        .build())
                .data(null)
                .build();
    }
}