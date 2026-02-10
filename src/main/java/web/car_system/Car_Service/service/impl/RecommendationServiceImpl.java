package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.dto.recommendation.CreateOrUpdateRuleRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRuleDto;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.RecommendationRule;
import web.car_system.Car_Service.domain.mapper.CarMapper;
import web.car_system.Car_Service.domain.mapper.InventoryCarMapper;
import web.car_system.Car_Service.domain.mapper.RecommendationRuleMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.RecommendationRuleRepository;
import web.car_system.Car_Service.service.RecommendationService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRuleRepository ruleRepository;
    private final InventoryCarRepository inventoryCarRepository;
    private final InventoryCarMapper inventoryCarMapper;
    private final CarRepository carRepository; // <-- THAY ĐỔI: Dùng CarRepository
    private final CarMapper carMapper;       // <-- THAY ĐỔI: Dùng CarMapper
    private final ObjectMapper objectMapper; // Spring Boot tự động cung cấp Bean này
    private final RecommendationRuleMapper ruleMapper;
    // === LOGIC GỢI Ý CHO KHÁCH HÀNG ===

    @Override
    @Transactional(readOnly = true)
    public Page<CarResponseDTO> findSuggestions(RecommendationRequest request, Pageable pageable) {
        List<RecommendationRule> activeRules = ruleRepository.findAllByIsActiveTrue();

        for (RecommendationRule rule : activeRules) {
            try {
                JsonNode conditionsNode = objectMapper.readTree(rule.getConditionsJson());
                if (matches(request.getCriteria(), conditionsNode)) {
                    JsonNode suggestionNode = objectMapper.readTree(rule.getSuggestionJson());
                    // GỌI PHƯƠNG THỨC HELPER MỚI VỚI PAGINATION
                    return findCarTemplatesBySuggestionPaginated(suggestionNode, pageable);
                }
            } catch (Exception e) {
                // Ghi log
                System.err.println("Lỗi parse JSON cho rule ID " + rule.getId() + ": " + e.getMessage());
            }
        }

        // Trả về Page rỗng nếu không match rule nào
        return Page.empty(pageable);
    }
    
    /**
     * Helper method với pagination support
     */
    private Page<CarResponseDTO> findCarTemplatesBySuggestionPaginated(JsonNode suggestionNode, Pageable pageable) {
        // Build Specification như cũ
        Specification<Car> spec = buildCarSpecification(suggestionNode);
        
        // Tìm tất cả cars matching (chưa phân trang)
        List<Car> allMatchingCars = carRepository.findAll(spec);
        
        // Deduplicate cars by name và map to DTO
        List<CarResponseDTO> uniqueCars = allMatchingCars.stream()
                .map(carMapper::toCarResponseDTO)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                dto -> dto.name(), // Key: Tên xe
                                dto -> dto,        // Value: DTO
                                (existing, replacement) -> {
                                    // Ưu tiên xe có thumbnail
                                    if (existing.thumbnail() == null && replacement.thumbnail() != null) {
                                        return replacement;
                                    }
                                    return existing;
                                },
                                LinkedHashMap::new // Giữ thứ tự
                        ),
                        map -> new ArrayList<>(map.values())
                ));
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueCars.size());
        
        List<CarResponseDTO> pageContent = start >= uniqueCars.size() ? 
            Collections.emptyList() : 
            uniqueCars.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, uniqueCars.size());
    }
    
    /**
     * Extract Specification building logic
     */
    private Specification<Car> buildCarSpecification(JsonNode suggestionNode) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Hãng xe (Manufacturer ID)
            if (suggestionNode.has("manufacturer_id")) {
                int manufId = suggestionNode.get("manufacturer_id").asInt();
                predicates.add(criteriaBuilder.equal(root.get("manufacturer").get("id"), manufId));
            }

            // 2. Lọc theo Loại động cơ (Engine Type)
            if (suggestionNode.has("engine_type")) {
                String engineType = suggestionNode.get("engine_type").asText();
                predicates.add(criteriaBuilder.equal(root.get("engineType"), engineType));
            }

            // 3. Lọc theo Giá (Price)
            if (suggestionNode.has("max_price")) {
                BigDecimal maxPrice = new BigDecimal(suggestionNode.get("max_price").asText());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Helper method để kiểm tra sự trùng khớp
    private boolean matches(Map<String, Object> userCriteria, JsonNode ruleConditions) {
        Iterator<Map.Entry<String, JsonNode>> fields = ruleConditions.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            String ruleValue = field.getValue().asText();

            // Nếu tiêu chí của người dùng không chứa key của rule, hoặc giá trị không khớp -> false
            if (!userCriteria.containsKey(key) || !userCriteria.get(key).toString().equals(ruleValue)) {
                return false;
            }
        }
        // Nếu tất cả các key/value trong rule đều khớp -> true
        return true;
    }

    // Helper method để tìm xe dựa trên gợi ý
    private List<InventoryCarDto> findCarsBySuggestion(JsonNode suggestionNode) {
        Specification<InventoryCar> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Phân khúc xe (Nằm trong bảng Car)
            if (suggestionNode.has("segment")) {
                String segment = suggestionNode.get("segment").asText();
                // Giả định: InventoryCar -> Car -> CarSegment -> name
                predicates.add(criteriaBuilder.equal(root.get("car").get("carSegment").get("name"), segment));
            }

            // 2. Lọc theo Số chỗ ngồi tối thiểu (Nằm trong bảng Car)
            if (suggestionNode.has("minSeats")) {
                int minSeats = suggestionNode.get("minSeats").asInt();
                // Giả định: InventoryCar -> Car -> seats
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("car").get("seats"), minSeats));
            }

            // 3. Lọc theo Giá tiền tối đa (InventoryCar.price)
            if (suggestionNode.has("maxPrice")) {
                BigDecimal maxPrice = new BigDecimal(suggestionNode.get("maxPrice").asText());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 4. Lọc theo Tình trạng xe: Mới/Cũ (InventoryCar.conditionType)
            if (suggestionNode.has("condition")) {
                String condition = suggestionNode.get("condition").asText();
                // So sánh chuỗi với Enum (convert Enum sang String trong câu query)
                predicates.add(criteriaBuilder.equal(root.get("conditionType").as(String.class), condition));
            }

            // 5. Lọc theo Năm sản xuất tối thiểu (InventoryCar.yearOfManufacture)
            if (suggestionNode.has("minYear")) {
                int minYear = suggestionNode.get("minYear").asInt();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("yearOfManufacture"), minYear));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Thực hiện truy vấn
        List<InventoryCar> foundCars = inventoryCarRepository.findAll(spec);

        // Mapping sang DTO
        return foundCars.stream()
                .map(inventoryCarMapper::toDto)
                .collect(Collectors.toList());
    }

    // === CÁC PHƯƠM THỨC CRUD CHO ADMIN ===

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationRuleDto> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationRuleDto getRuleById(Long id) {
        return ruleRepository.findById(id)
                .map(ruleMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quy tắc với ID: " + id));
    }

    @Override
    @Transactional
    public RecommendationRuleDto createRule(CreateOrUpdateRuleRequest request) {
        // Kiểm tra tên rule đã tồn tại chưa
        if (ruleRepository.existsByRuleName(request.getRuleName())) {
            throw new IllegalArgumentException("Tên quy tắc '" + request.getRuleName() + "' đã tồn tại.");
        }

        RecommendationRule newRule = ruleMapper.toEntity(request);
        RecommendationRule savedRule = ruleRepository.save(newRule);
        return ruleMapper.toDto(savedRule);
    }

    @Override
    @Transactional
    public RecommendationRuleDto updateRule(Long id, CreateOrUpdateRuleRequest request) {
        // Tìm quy tắc hiện có
        RecommendationRule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không thể cập nhật. Không tìm thấy quy tắc với ID: " + id));

        // Kiểm tra nếu đổi tên, đảm bảo tên mới không bị trùng với rule khác
        if (!existingRule.getRuleName().equals(request.getRuleName())) {
            if(ruleRepository.existsByRuleName(request.getRuleName())) {
                throw new IllegalArgumentException("Tên quy tắc '" + request.getRuleName() + "' đã tồn tại.");
            }
        }

        // Cập nhật các trường
        existingRule.setRuleName(request.getRuleName());
        existingRule.setDescription(request.getDescription());
        existingRule.setConditionsJson(ruleMapper.toString(request.getConditionsJson())); // Dùng helper từ mapper
        existingRule.setSuggestionJson(ruleMapper.toString(request.getSuggestionJson()));
        existingRule.setActive(request.isActive());

        RecommendationRule updatedRule = ruleRepository.save(existingRule);
        return ruleMapper.toDto(updatedRule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new EntityNotFoundException("Không thể xóa. Không tìm thấy quy tắc với ID: " + id);
        }
        ruleRepository.deleteById(id);
    }

    // (Tôi cũng đã sửa lại tên biến recommendationRuleMapper -> ruleMapper cho ngắn gọn
    // và thêm RecommendationRuleRepository để kiểm tra tên trùng lặp)
}