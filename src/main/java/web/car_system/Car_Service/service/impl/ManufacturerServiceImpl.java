package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerCreateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerUpdateDTO;
import web.car_system.Car_Service.domain.dto.manufacturer.ManufacturerResponseDTO;
import web.car_system.Car_Service.domain.entity.Manufacturer;
import web.car_system.Car_Service.domain.mapper.ManufacturerMapper;
import web.car_system.Car_Service.repository.ManufacturerRepository;
import web.car_system.Car_Service.service.ImageService;
import web.car_system.Car_Service.service.ManufacturerService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {
    private final ManufacturerRepository repository;
    private final ManufacturerMapper mapper;
    private final ImageService imageUploadService;
    @Override
    public GlobalResponseDTO<?, ManufacturerResponseDTO> createManufacturer(ManufacturerCreateDTO createDTO) throws IOException, IOException {
        // Upload thumbnail
        String thumbnailUrl = imageUploadService.uploadManufacturerThumbnail(createDTO.thumbnailFile());

        // Check duplicate name
        if (repository.existsByName(createDTO.name())) {
            throw new RuntimeException("Tên hãng sản xuất đã tồn tại");
        }

        // Create manufacturer
        Manufacturer manufacturer = Manufacturer.builder()
                .name(createDTO.name())
                .thumbnail(thumbnailUrl)
                .build();

        Manufacturer savedManufacturer = repository.save(manufacturer);

        return GlobalResponseDTO.<NoPaginatedMeta, ManufacturerResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Tạo hãng sản xuất thành công")
                        .build())
                .data(mapper.toResponseDTO(savedManufacturer))
                .build();
    }


    @Override
    public GlobalResponseDTO<PaginatedMeta, List<ManufacturerResponseDTO>> getAllManufacturers(Pageable pageable) {
        Page<Manufacturer> page = repository.findAll(pageable);

        Pagination pagination = Pagination.builder()
                .pageIndex(page.getNumber())
                .pageSize((short) page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        List<ManufacturerResponseDTO> manufacturers = page.getContent().stream()
                .map(mapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<PaginatedMeta, List<ManufacturerResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Manufacturers retrieved successfully")
                        .pagination(pagination)
                        .build())
                .data(manufacturers)
                .build();
    }

    @Override
    public GlobalResponseDTO<?, ManufacturerResponseDTO> updateManufacturer(Integer manufacturerId, ManufacturerUpdateDTO updateDTO) throws IOException {
        Manufacturer manufacturer = repository.findById(manufacturerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng sản xuất"));

        // Update thumbnail if new file provided
        if (updateDTO.thumbnailFile() != null && !updateDTO.thumbnailFile().isEmpty()) {
            String newThumbnailUrl = imageUploadService.uploadManufacturerThumbnail(updateDTO.thumbnailFile());
            manufacturer.setThumbnail(newThumbnailUrl);
        }

        // Update name
        manufacturer.setName(updateDTO.name());

        Manufacturer updatedManufacturer = repository.save(manufacturer);

        return GlobalResponseDTO.<NoPaginatedMeta, ManufacturerResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Cập nhật hãng sản xuất thành công")
                        .build())
                .data(mapper.toResponseDTO(updatedManufacturer))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, Void> deleteManufacturer(Integer manufacturerId) {
        repository.deleteById(manufacturerId);

        return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Manufacturer deleted successfully")
                        .build())
                .data(null)
                .build();
    }
}
