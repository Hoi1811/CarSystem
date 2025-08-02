package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.dto.global.*;
import web.car_system.Car_Service.domain.dto.attribute.AttributeSearchRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;
import web.car_system.Car_Service.domain.dto.specification.SpecificationOnlyResponseDTO;
import web.car_system.Car_Service.domain.entity.Specification;
import web.car_system.Car_Service.repository.SpecificationRepository;
import web.car_system.Car_Service.service.SpecificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecificationServiceImpl implements SpecificationService {
    private final SpecificationRepository specificationRepository;

    @Override
    public Specification createSpecification(String name) {
        Specification specification = new Specification();
        specification.setName(name);
        return specificationRepository.save(specification);
    }

    @Override
    public Specification updateSpecification(Integer id, String name) {
        Specification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specification not found"));
        specification.setName(name);
        return specificationRepository.save(specification);
    }

    @Override
    public void deleteSpecification(Integer id) {
        Specification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specification not found"));
        specificationRepository.delete(specification);
    }
    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<SpecificationOnlyResponseDTO>> findAllSpecificationsWithLimitedAttributes() {


        // Lấy danh sách Specification với Attributes phân trang
        List<Object[]> results = specificationRepository.findAllSpecificationsWithLimitedAttributes();

        // Ánh xạ kết quả
        Map<Integer, SpecificationOnlyResponseDTO> specMap = new HashMap<>();
        for (Object[] row : results) {
            Integer specId = (Integer) row[0];
            String specName = (String) row[1];
            Integer attrId = row[2] != null ? ((Number) row[2]).intValue() : null;
            String attrName = (String) row[3];

            SpecificationOnlyResponseDTO specDTO = specMap.computeIfAbsent(specId, id ->
                    new SpecificationOnlyResponseDTO(id, specName, new ArrayList<>()));

            if (attrId != null && attrName != null) {
                specDTO.attributes().add(new AttributeOnlyResponseDTO(attrId, attrName, null, null));
            }
        }

        List<SpecificationOnlyResponseDTO> specDTOs = new ArrayList<>(specMap.values());


        return GlobalResponseDTO.<NoPaginatedMeta, List<SpecificationOnlyResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy danh sách thông số kỹ thuật thành công")
                        .build())
                .data(specDTOs)
                .build();
    }

    @Override
    public GlobalResponseDTO<PaginatedMeta, List<AttributeOnlyResponseDTO>> getAttributesBySpecificationId(AttributeSearchRequestDTO request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        Integer specId = request.specificationId();
        String keyword = request.keyword();

        // Gọi phương thức chung
        Page<AttributeOnlyResponseDTO> attributePage = specificationRepository.findAttributesBySpecificationIdAndOptionalKeyword(
                specId, keyword, pageable);

        List<AttributeOnlyResponseDTO> attributeDTOs = attributePage.getContent();

        Pagination paginationInfo = Pagination.builder()
                .pageIndex(attributePage.getNumber())
                .pageSize((short) attributePage.getSize())
                .totalItems(attributePage.getTotalElements())
                .totalPages(attributePage.getTotalPages())
                .build();

        String message = keyword == null || keyword.isEmpty()
                ? "Lấy danh sách thuộc tính của thông số kỹ thuật thành công"
                : "Tìm kiếm thuộc tính của thông số kỹ thuật thành công";

        return GlobalResponseDTO.<PaginatedMeta, List<AttributeOnlyResponseDTO>>builder()
                .meta(PaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message(message)
                        .pagination(paginationInfo)
                        .build())
                .data(attributeDTOs)
                .build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, List<SpecificationOnlyResponseDTO>> getFormSchema() {
        // Gọi query mới
        List<Object[]> results = specificationRepository.findFormSchemaData();

        // Logic ánh xạ gần như giữ nguyên, chỉ cần đọc thêm 2 cột mới
        Map<Integer, SpecificationOnlyResponseDTO> specMap = new HashMap<>();
        for (Object[] row : results) {
            Integer specId = (Integer) row[0];
            String specName = (String) row[1];
            Integer attrId = row[2] != null ? ((Number) row[2]).intValue() : null;
            String attrName = (String) row[3];
            String controlType = (String) row[4]; // <-- LẤY DỮ LIỆU MỚI
            String optionsSource = (String) row[5]; // <-- LẤY DỮ LIỆU MỚI

            SpecificationOnlyResponseDTO specDTO = specMap.computeIfAbsent(specId, id ->
                    new SpecificationOnlyResponseDTO(id, specName, new ArrayList<>()));

            if (attrId != null && attrName != null) {
                // Tạo DTO mới với các trường đã được thêm vào
                specDTO.attributes().add(
                        new AttributeOnlyResponseDTO(attrId, attrName, controlType, optionsSource)
                );
            }
        }

        List<SpecificationOnlyResponseDTO> specDTOs = new ArrayList<>(specMap.values());

        // Trả về kết quả trong GlobalResponseDTO như cũ
        return GlobalResponseDTO.<NoPaginatedMeta, List<SpecificationOnlyResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder()
                        .status(Status.SUCCESS)
                        .message("Lấy cấu trúc form thành công")
                        .build())
                .data(specDTOs)
                .build();
    }
}
