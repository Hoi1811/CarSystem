package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupUpdateDTO;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupResponseDTO;
import web.car_system.Car_Service.domain.entity.CarSegmentGroup;
import web.car_system.Car_Service.domain.mapper.CarSegmentGroupMapper;
import web.car_system.Car_Service.repository.CarSegmentGroupRepository;
import web.car_system.Car_Service.service.CarSegmentGroupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarSegmentGroupServiceImpl implements CarSegmentGroupService {
    private final CarSegmentGroupRepository groupRepository;
    private final CarSegmentGroupMapper groupMapper;

    @Override
    public GlobalResponseDTO<?, CarSegmentGroupResponseDTO> createGroup(CarSegmentGroupCreateDTO createDTO) {
        if (groupRepository.existsByName(createDTO.name())) {
            throw new RuntimeException("Tên nhóm phân khúc đã tồn tại");
        }

        CarSegmentGroup group = CarSegmentGroup.builder()
                .name(createDTO.name())
                .description(createDTO.description())
                .build();

        CarSegmentGroup savedGroup = groupRepository.save(group);

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentGroupResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Tạo nhóm phân khúc thành công")
                        .build())
                .data(groupMapper.toResponseDTO(savedGroup))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, CarSegmentGroupResponseDTO> getGroupById(Integer id) {
        CarSegmentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm phân khúc"));

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentGroupResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy thông tin nhóm phân khúc thành công")
                        .build())
                .data(groupMapper.toResponseDTO(group))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, List<CarSegmentGroupResponseDTO>> getAllGroups() {
        List<CarSegmentGroupResponseDTO> groups = groupRepository.findAll().stream()
                .map(groupMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<NoPaginatedMeta, List<CarSegmentGroupResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách nhóm phân khúc thành công")
                        .build())
                .data(groups)
                .build();
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<CarSegmentGroupResponseDTO>> getAllGroups(Pageable pageable) {
        Page<CarSegmentGroup> page = groupRepository.findAll(pageable);

        Pagination pagination = Pagination.builder()
                .pageIndex(page.getNumber())
                .pageSize((short) page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        List<CarSegmentGroupResponseDTO> groups = page.getContent().stream()
                .map(groupMapper::toResponseDTO)
                .toList();

        return GlobalResponseDTO.<PaginatedMeta, List<CarSegmentGroupResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách nhóm phân khúc thành công")
                        .pagination(pagination)
                        .build())
                .data(groups)
                .build();
    }

    @Override
    public GlobalResponseDTO<?, CarSegmentGroupResponseDTO> updateGroup(Integer id, CarSegmentGroupUpdateDTO updateDTO) {
        CarSegmentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm phân khúc"));

        if (!group.getName().equals(updateDTO.name()) &&
                groupRepository.existsByName(updateDTO.name())) {
            throw new RuntimeException("Tên nhóm phân khúc đã tồn tại");
        }

        group.setName(updateDTO.name());
        group.setDescription(updateDTO.description());

        CarSegmentGroup updatedGroup = groupRepository.save(group);

        return GlobalResponseDTO.<NoPaginatedMeta, CarSegmentGroupResponseDTO>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Cập nhật nhóm phân khúc thành công")
                        .build())
                .data(groupMapper.toResponseDTO(updatedGroup))
                .build();
    }

    @Override
    public GlobalResponseDTO<?, Void> deleteGroup(Integer id) {
        if (groupRepository.isGroupInUse(id)) {
            throw new RuntimeException("Không thể xóa nhóm phân khúc đang được sử dụng");
        }

        groupRepository.deleteById(id);
        return GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Xóa nhóm phân khúc thành công")
                        .build())
                .data(null)
                .build();
    }
}