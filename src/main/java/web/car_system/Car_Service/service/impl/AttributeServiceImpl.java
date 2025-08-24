package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.dto.attribute.AttributeRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeResponseDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderResponseDTO;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.repository.*;
import web.car_system.Car_Service.service.AttributeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Mặc định các phương thức là chỉ đọc, tăng hiệu năng
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final CarAttributeRepository carAttributeRepository; // Để kiểm tra khi xóa
    private final SpecificationRepository specificationRepository;
    private final ComparisonRuleRepository comparisonRuleRepository;
    private final AttributeEnumOrderRepository enumOrderRepository;

    // Mapper (nếu bạn dùng MapStruct, inject nó vào)
    // private final AttributeMapper attributeMapper;

    @Override
    @Transactional // Ghi đè readOnly=true, cho phép ghi vào DB
    public AttributeResponseDTO createAttribute(AttributeRequestDTO requestDTO) {
        // Kiểm tra xem tên attribute đã tồn tại chưa
        if (attributeRepository.existsByName(requestDTO.name())) {
            throw new IllegalArgumentException("Tên thuộc tính '" + requestDTO.name() + "' đã tồn tại.");
        }

        // Chuyển đổi từ DTO sang Entity
        Attribute attribute = new Attribute();
        mapDtoToEntity(attribute, requestDTO);

        // Lưu vào DB
        Attribute savedAttribute = attributeRepository.save(attribute);

        // Chuyển từ Entity đã lưu sang Response DTO và trả về
        return AttributeResponseDTO.fromEntity(savedAttribute);
    }

    @Override
    public AttributeResponseDTO getAttributeById(Integer id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thuộc tính với ID: " + id));
        return AttributeResponseDTO.fromEntity(attribute);
    }

    @Override
    public List<AttributeResponseDTO> getAllAttributes() {
        return attributeRepository.findAll().stream()
                .map(AttributeResponseDTO::fromEntity) // Dùng factory method đã tạo
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttributeResponseDTO updateAttribute(Integer id, AttributeRequestDTO requestDTO) {
        Attribute existingAttribute = attributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thuộc tính với ID: " + id));

        // Kiểm tra nếu tên mới đã bị thuộc tính khác sử dụng
        attributeRepository.findByName(requestDTO.name()).ifPresent(attr -> {
            if (!attr.getAttributeId().equals(id)) {
                throw new IllegalArgumentException("Tên thuộc tính '" + requestDTO.name() + "' đã được sử dụng.");
            }
        });

        // Cập nhật các trường
        mapDtoToEntity(existingAttribute, requestDTO);

        Attribute updatedAttribute = attributeRepository.save(existingAttribute);
        return AttributeResponseDTO.fromEntity(updatedAttribute);
    }

    @Override
    @Transactional
    public void deleteAttribute(Integer id) {
        // 1. Kiểm tra xem attribute có tồn tại không
        if (!attributeRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy thuộc tính với ID: " + id);
        }

        // 2. Logic "Xóa An toàn": Kiểm tra xem attribute có đang được xe nào sử dụng không
        if (carAttributeRepository.existsByAttribute_AttributeId(id)) {
            throw new IllegalStateException("Không thể xóa thuộc tính này vì nó đang được sử dụng bởi một hoặc nhiều xe.");
        }

        // Nếu không có xe nào sử dụng, tiến hành xóa
        // Note: Cần xử lý cả việc xóa trong `attribute_enum_orders` nếu có
        attributeRepository.deleteById(id);
    }

    // --- Helper Method ---

    /**
     * Hàm private để tái sử dụng logic map DTO sang Entity cho cả create và update
     */
    private void mapDtoToEntity(Attribute attribute, AttributeRequestDTO requestDTO) {
        Specification spec = specificationRepository.findById(requestDTO.specificationId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhóm thông số với ID: " + requestDTO.specificationId()));

        ComparisonRule rule = comparisonRuleRepository.findById(requestDTO.comparisonRuleId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy luật so sánh với ID: " + requestDTO.comparisonRuleId()));

        attribute.setName(requestDTO.name());
        attribute.setDescription(requestDTO.description());
        attribute.setControlType(requestDTO.controlType());
        attribute.setUnit(requestDTO.unit());
        attribute.setWeight(requestDTO.weight());
        attribute.setSpecification(spec);
        attribute.setComparisonRule(rule);

        // QUY TẮC NGHIỆP VỤ: Tự động gán optionsSource nếu là SINGLE_SELECT
        if ("SINGLE_SELECT".equals(requestDTO.controlType())) {
            attribute.setOptionsSource(requestDTO.name());
        } else {
            attribute.setOptionsSource(null); // Clear nếu không phải là SINGLE_SELECT
        }
    }
    @Override
    @Transactional
    public EnumOrderResponseDTO addOptionToAttribute(Integer attributeId, EnumOrderRequestDTO requestDTO) {
        // 1. Lấy Attribute cha và kiểm tra
        Attribute attribute = findAttributeByIdAndCheckType(attributeId);

        // 2. Kiểm tra xem key đã tồn tại cho attribute này chưa
        AttributeEnumOrderId id = new AttributeEnumOrderId(attributeId, requestDTO.valueKey());
        if (enumOrderRepository.existsById(id)) {
            throw new IllegalArgumentException("Key '" + requestDTO.valueKey() + "' đã tồn tại cho thuộc tính này.");
        }

        // 3. Tạo và lưu entity mới
        AttributeEnumOrder newOption = new AttributeEnumOrder();
        newOption.setId(id);
        newOption.setAttribute(attribute);
        newOption.setDisplayValue(requestDTO.displayValue());
        newOption.setRank(requestDTO.rank());

        AttributeEnumOrder savedOption = enumOrderRepository.save(newOption);
        return EnumOrderResponseDTO.fromEntity(savedOption);
    }

    @Override
    public List<EnumOrderResponseDTO> getOptionsForAttribute(Integer attributeId) {
        // Không cần kiểm tra type, có thể muốn xem options của mọi attribute
        List<AttributeEnumOrder> options = enumOrderRepository.findById_AttributeIdOrderByRankAsc(attributeId);
        return options.stream()
                .map(EnumOrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnumOrderResponseDTO updateOptionForAttribute(Integer attributeId, String valueKey, EnumOrderRequestDTO requestDTO) {
        // 1. Lấy Attribute cha và kiểm tra
        findAttributeByIdAndCheckType(attributeId);

        // 2. Lấy Option cần cập nhật
        AttributeEnumOrderId id = new AttributeEnumOrderId(attributeId, valueKey);
        AttributeEnumOrder existingOption = enumOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy option với key '" + valueKey + "' cho thuộc tính này."));

        // 3. Cập nhật các trường (không cho đổi key, vì key là 1 phần của ID)
        existingOption.setDisplayValue(requestDTO.displayValue());
        existingOption.setRank(requestDTO.rank());

        // 4. Lưu lại
        AttributeEnumOrder updatedOption = enumOrderRepository.save(existingOption);
        return EnumOrderResponseDTO.fromEntity(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOptionFromAttribute(Integer attributeId, String valueKey) {
        AttributeEnumOrderId id = new AttributeEnumOrderId(attributeId, valueKey);

        // Logic "Xóa An toàn"
        if (!enumOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy option với key '" + valueKey + "' để xóa.");
        }

        // Tùy chọn: Kiểm tra xem valueKey này có đang được xe nào dùng không
        // if(carAttributeRepository.existsByAttribute_AttributeIdAndValue(attributeId, valueKey)) {
        //     throw new IllegalStateException("Không thể xóa option này vì đang có xe sử dụng.");
        // }

        enumOrderRepository.deleteById(id);
    }

    // --- Helper Method ---

    private Attribute findAttributeByIdAndCheckType(Integer attributeId) {
        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thuộc tính với ID: " + attributeId));

        if (!"SINGLE_SELECT".equals(attribute.getControlType())) {
            throw new IllegalArgumentException("Thuộc tính '" + attribute.getName() + "' không phải là loại SINGLE_SELECT.");
        }
        return attribute;
    }
}