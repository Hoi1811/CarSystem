package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentBatchCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentResponseDTO;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.CarSegmentMapper;
import web.car_system.Car_Service.repository.*;
import web.car_system.Car_Service.service.CarSegmentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarSegmentServiceImpl implements CarSegmentService {
    private final CarSegmentRepository segmentRepository;
    private final CarSegmentGroupRepository groupRepository;
    private final CarSegmentMapper segmentMapper;

    @Override
    public GlobalResponseDTO<?, CarSegmentResponseDTO> createSegment(CarSegmentCreateDTO createDTO) {
        if (segmentRepository.existsByName(createDTO.name())) {
            throw new RuntimeException("Tên phân khúc đã tồn tại");
        }

        CarSegmentGroup group = groupRepository.findById(createDTO.groupId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm phân khúc"));

        CarSegment segment = CarSegment.builder()
                .name(createDTO.name())
                .description(createDTO.description())
                .group(group)
                .build();

        CarSegment savedSegment = segmentRepository.save(segment);

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Tạo phân khúc xe thành công")
                        .build())
                .data(segmentMapper.toResponseDTO(savedSegment))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, CarSegmentResponseDTO> getSegmentById(Integer segmentId) {
        CarSegment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân khúc xe"));

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy thông tin phân khúc xe thành công")
                        .build())
                .data(segmentMapper.toResponseDTO(segment))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, List<CarSegmentResponseDTO>> getAllSegments() {
        List<CarSegmentResponseDTO> segments = segmentRepository.findAll().stream()
                .map(segmentMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarSegmentResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách phân khúc xe thành công")
                        .build())
                .data(segments)
                .build();
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarSegmentResponseDTO>> getAllSegments(Pageable pageable) {
        Page<CarSegment> page = segmentRepository.findAll(pageable);

        Pagination pagination = Pagination.builder()
                .pageIndex(page.getNumber())
                .pageSize((short) page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        List<CarSegmentResponseDTO> segments = page.getContent().stream()
                .map(segmentMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<PaginatedMeta, List<CarSegmentResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách phân khúc xe thành công")
                        .pagination(pagination)
                        .build())
                .data(segments)
                .build();
    }

    @Override
    public GlobalResponseDTO<?, List<CarSegmentResponseDTO>> getSegmentsByGroup(Integer groupId) {
        List<CarSegmentResponseDTO> segments = segmentRepository.findByGroupId(groupId).stream()
                .map(segmentMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarSegmentResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách phân khúc theo nhóm thành công")
                        .build())
                .data(segments)
                .build();
    }

    @Override
    @Transactional
    public GlobalResponseDTO<?, CarSegmentResponseDTO> updateSegment(Integer segmentId, CarSegmentUpdateDTO updateDTO) {
        CarSegment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân khúc xe"));

        if (!segment.getName().equals(updateDTO.name()) &&
                segmentRepository.existsByName(updateDTO.name())) {
            throw new RuntimeException("Tên phân khúc đã tồn tại");
        }

        CarSegmentGroup group = groupRepository.findById(updateDTO.groupId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm phân khúc"));

        segment.setName(updateDTO.name());
        segment.setDescription(updateDTO.description());
        segment.setGroup(group);

        CarSegment updatedSegment = segmentRepository.save(segment);

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Cập nhật phân khúc xe thành công")
                        .build())
                .data(segmentMapper.toResponseDTO(updatedSegment))
                .build();
    }

    @Override
    @Transactional
    public GlobalResponseDTO<?, Void> deleteSegment(Integer segmentId) {
        if (segmentRepository.isSegmentInUse(segmentId)) {
            throw new RuntimeException("Không thể xóa phân khúc đang được sử dụng");
        }

        segmentRepository.deleteById(segmentId);
        return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Xóa phân khúc xe thành công")
                        .build())
                .data(null)
                .build();
    }
    @Override
    @Transactional
    public GlobalResponseDTO<?, List<CarSegmentResponseDTO>> createBatchSegments(CarSegmentBatchCreateDTO batchCreateDTO) {
        // Kiểm tra group tồn tại
        CarSegmentGroup group = groupRepository.findById(batchCreateDTO.groupId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm phân khúc"));

        // Kiểm tra trùng tên segment trước khi tạo
        List<String> existingNames = segmentRepository.findByGroupIdAndNameIn(
                batchCreateDTO.groupId(),
                batchCreateDTO.segments().stream()
                        .map(CarSegmentBatchCreateDTO.CarSegmentItemDTO::name)
                        .collect(Collectors.toList())
        );

        if (!existingNames.isEmpty()) {
            throw new RuntimeException("Các tên phân khúc đã tồn tại: " + String.join(", ", existingNames));
        }

        // Tạo các segment
        List<CarSegment> newSegments = batchCreateDTO.segments().stream()
                .map(item -> CarSegment.builder()
                        .name(item.name())
                        .description(item.description())
                        .group(group)
                        .build())
                .collect(Collectors.toList());

        List<CarSegment> savedSegments = segmentRepository.saveAll(newSegments);

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarSegmentResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Tạo hàng loạt phân khúc xe thành công")
                        .build())
                .data(savedSegments.stream()
                        .map(segmentMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }
}