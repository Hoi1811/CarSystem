package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.recommendation.CreateOrUpdateRuleRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRequest;
import web.car_system.Car_Service.domain.dto.recommendation.RecommendationRuleDto;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.RecommendationRule;
import web.car_system.Car_Service.domain.mapper.CarMapper;
import web.car_system.Car_Service.domain.mapper.RecommendationRuleMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.RecommendationRuleRepository;
import web.car_system.Car_Service.service.RecommendationService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRuleRepository ruleRepository;
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final ObjectMapper objectMapper;
    private final RecommendationRuleMapper ruleMapper;

    // Ngân sách FE gửi đơn vị triệu VNĐ, DB lưu đơn vị VNĐ → nhân 1.000.000
    private static final BigDecimal BUDGET_MULTIPLIER = new BigDecimal("1000000");

    // === LOGIC GỢI Ý CHO KHÁCH HÀNG (Hybrid: Direct mapping + Usage rule) ===

    @Override
    @Transactional(readOnly = true)
    public Page<CarResponseDTO> findSuggestions(RecommendationRequest request, Pageable pageable) {
        Map<String, Object> criteria = request.getCriteria();

        // Xây dựng Specification từ tiêu chí khách hàng
        Specification<Car> spec = buildSpecFromCriteria(criteria);

        // Tìm tất cả xe matching
        List<Car> allMatchingCars = carRepository.findAll(spec);

        // Loại trùng theo tên xe, ưu tiên xe có thumbnail
        List<CarResponseDTO> uniqueCars = allMatchingCars.stream()
                .map(carMapper::toCarResponseDTO)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                dto -> dto.name(),
                                dto -> dto,
                                (existing, replacement) -> {
                                    if (existing.thumbnail() == null && replacement.thumbnail() != null) {
                                        return replacement;
                                    }
                                    return existing;
                                },
                                LinkedHashMap::new
                        ),
                        map -> new ArrayList<>(map.values())
                ));

        // Phân trang thủ công (do dedup trên Java)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueCars.size());

        List<CarResponseDTO> pageContent = start >= uniqueCars.size()
                ? Collections.emptyList()
                : uniqueCars.subList(start, end);

        return new PageImpl<>(pageContent, pageable, uniqueCars.size());
    }

    /**
     * Xây dựng Specification trực tiếp từ tiêu chí khách hàng.
     * - budget     → price BETWEEN (×1.000.000)
     * - numberOfPassengers → seats >=
     * - usage      → tra admin rule → lọc theo segments + driveTrain
     */
    private Specification<Car> buildSpecFromCriteria(Map<String, Object> criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Ngân sách → khoảng giá
            if (criteria.containsKey("budget")) {
                parseBudgetToPrice(criteria.get("budget").toString(), root, cb, predicates);
            }

            // 2. Số hành khách → số chỗ ngồi tối thiểu
            if (criteria.containsKey("numberOfPassengers")) {
                try {
                    int seats = Integer.parseInt(criteria.get("numberOfPassengers").toString());
                    predicates.add(cb.greaterThanOrEqualTo(root.get("seats"), seats));
                } catch (NumberFormatException ignored) {
                }
            }

            // 3. Nhu cầu sử dụng → tra rule admin để lấy phân khúc + bộ lọc phụ
            if (criteria.containsKey("usage")) {
                applyUsageRule(criteria.get("usage").toString(), root, cb, predicates);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Parse chuỗi budget (VD: "500-700") sang predicate price BETWEEN.
     * Budget đơn vị triệu VNĐ, DB lưu VNĐ → nhân BUDGET_MULTIPLIER.
     */
    private void parseBudgetToPrice(String budget, Root<Car> root, CriteriaBuilder cb,
                                    List<Predicate> predicates) {
        String[] parts = budget.split("-");
        if (parts.length == 2) {
            try {
                BigDecimal minPrice = new BigDecimal(parts[0].trim()).multiply(BUDGET_MULTIPLIER);
                BigDecimal maxPrice = new BigDecimal(parts[1].trim()).multiply(BUDGET_MULTIPLIER);
                predicates.add(cb.between(root.get("price"), minPrice, maxPrice));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    /**
     * Tra bảng rule để tìm rule ứng với usage → lấy segments + driveTrain từ suggestionJson.
     */
    private void applyUsageRule(String usage, Root<Car> root, CriteriaBuilder cb,
                                List<Predicate> predicates) {
        List<RecommendationRule> activeRules = ruleRepository.findAllByIsActiveTrue();

        for (RecommendationRule rule : activeRules) {
            try {
                JsonNode conditions = objectMapper.readTree(rule.getConditionsJson());
                if (conditions.has("usage") && conditions.get("usage").asText().equals(usage)) {
                    JsonNode suggestion = objectMapper.readTree(rule.getSuggestionJson());

                    // Lọc theo phân khúc
                    if (suggestion.has("segments") && suggestion.get("segments").isArray()) {
                        List<String> segments = new ArrayList<>();
                        suggestion.get("segments").forEach(node -> segments.add(node.asText()));
                        if (!segments.isEmpty()) {
                            predicates.add(root.get("carSegment").get("name").in(segments));
                        }
                    }

                    // Lọc theo hệ dẫn động (tuỳ chọn)
                    if (suggestion.has("driveTrain") && !suggestion.get("driveTrain").asText().isBlank()) {
                        predicates.add(cb.equal(root.get("driveTrain"), suggestion.get("driveTrain").asText()));
                    }

                    break; // Đã tìm thấy rule phù hợp
                }
            } catch (Exception e) {
                System.err.println("Lỗi parse JSON cho rule ID " + rule.getId() + ": " + e.getMessage());
            }
        }
    }

    // === CÁC PHƯƠNG THỨC CRUD CHO ADMIN ===

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
        RecommendationRule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không thể cập nhật. Không tìm thấy quy tắc với ID: " + id));

        if (!existingRule.getRuleName().equals(request.getRuleName())) {
            if (ruleRepository.existsByRuleName(request.getRuleName())) {
                throw new IllegalArgumentException("Tên quy tắc '" + request.getRuleName() + "' đã tồn tại.");
            }
        }

        existingRule.setRuleName(request.getRuleName());
        existingRule.setDescription(request.getDescription());
        existingRule.setConditionsJson(ruleMapper.toString(request.getConditionsJson()));
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
}