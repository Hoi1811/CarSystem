package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.comparison.*;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.repository.AttributeEnumOrderRepository;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.service.ComparisonService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComparisonServiceImpl implements ComparisonService {

    private final CarRepository carRepository;
    private final AttributeEnumOrderRepository enumOrderRepository;

    @Override
    public ComparisonResultDTO compareCars(List<Integer> carIds) {
        if (carIds == null || carIds.size() < 2) {
            throw new IllegalArgumentException("Cần ít nhất 2 xe để so sánh.");
        }

        // 1. Lấy dữ liệu các xe từ DB và kiểm tra
        List<Car> cars = carRepository.findAllByCarIdIn(carIds);
        if (cars.size() < carIds.size()) {
            List<Integer> foundIds = cars.stream().map(Car::getCarId).toList();
            String missingIds = carIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new EntityNotFoundException("Không tìm thấy các xe có ID: " + missingIds);
        }

        // 2. Thu thập TẤT CẢ các thuộc tính từ các xe được chọn, loại bỏ trùng lặp
        List<Attribute> allAttributesToCompare = cars.stream()
                .flatMap(car -> car.getCarAttributes().stream().map(CarAttribute::getAttribute))
                .distinct()
                .sorted(Comparator.comparing(Attribute::getAttributeId))
                .collect(Collectors.toList());
        log.info("So sánh {} xe với tổng số {} thuộc tính duy nhất.", cars.size(), allAttributesToCompare.size());

        // 3. Khởi tạo map điểm số
        Map<Integer, Float> carScores = carIds.stream().collect(Collectors.toMap(id -> id, id -> 0.0f));

        // 4. Thực hiện so sánh cho từng thuộc tính
        List<AttributeComparisonDTO> attributeComparisons = new ArrayList<>();
        for (Attribute attribute : allAttributesToCompare) {
            Map<Integer, CarAttribute> carAttributeMap = getCarAttributeMapForAllCars(carIds, cars, attribute.getAttributeId());

            List<AttributeValueComparisonDTO> comparedValues = compareAttributeValues(attribute, carAttributeMap, carScores);

            // Chỉ thêm vào kết quả nếu việc so sánh có ý nghĩa (không phải tất cả đều "—")
            boolean hasMeaningfulValue = comparedValues.stream().anyMatch(dto -> !"—".equals(dto.displayValue()));
            if (hasMeaningfulValue) {
                attributeComparisons.add(
                        AttributeComparisonDTO.builder()
                                .attributeName(attribute.getName())
                                .unit(attribute.getUnit())
                                .comparedValues(comparedValues)
                                .build()
                );
            }
        }

        // 5. Xây dựng Profile DTO với điểm số cuối cùng
        List<CarComparisonProfileDTO> carProfiles = cars.stream()
                .map(car -> CarComparisonProfileDTO.builder()
                        .carId(car.getCarId())
                        .name(car.getName())
                        .model(car.getModel())
                        .thumbnail(car.getThumbnail())
                        .price(car.getPrice())
                        .totalScore(Math.round(carScores.getOrDefault(car.getCarId(), 0.0f) * 100.0f) / 100.0f)
                        .build())
                .sorted(Comparator.comparing(CarComparisonProfileDTO::carId)) // Sắp xếp để nhất quán
                .collect(Collectors.toList());

        // 6. Nhóm các kết quả so sánh theo Specification
        List<SpecificationComparisonDTO> specComparisons = groupComparisonsBySpecification(attributeComparisons, allAttributesToCompare);

        // 7. Trả về kết quả cuối cùng
        return new ComparisonResultDTO(carProfiles, specComparisons);
    }

    /**
     * Helper: Từ list xe, lấy ra map <carId, carAttribute> cho 1 attributeId cụ thể.
     * Đảm bảo trả về entry cho tất cả carId được yêu cầu, kể cả khi xe không có thuộc tính đó (value=null).
     */
    private Map<Integer, CarAttribute> getCarAttributeMapForAllCars(List<Integer> requestedCarIds, List<Car> loadedCars, Integer attributeId) {
        Map<Integer, CarAttribute> carAttributeMap = new HashMap<>();
        Map<Integer, Car> loadedCarsMap = loadedCars.stream().collect(Collectors.toMap(Car::getCarId, car -> car));

        for (Integer carId : requestedCarIds) {
            Car car = loadedCarsMap.get(carId);
            if (car != null) {
                CarAttribute foundAttr = car.getCarAttributes().stream()
                        .filter(ca -> ca.getAttribute().getAttributeId().equals(attributeId))
                        .findFirst()
                        .orElse(null);
                carAttributeMap.put(carId, foundAttr);
            } else {
                carAttributeMap.put(carId, null); // Xe này có thể không được load, entry vẫn là null
            }
        }
        return carAttributeMap;
    }

    /**
     * So sánh giá trị của một thuộc tính, xử lý thiếu dữ liệu và cập nhật điểm số.
     */
    private List<AttributeValueComparisonDTO> compareAttributeValues(
            Attribute attribute,
            Map<Integer, CarAttribute> carAttributeMap,
            Map<Integer, Float> carScores) {

        // 1. Lọc ra những xe có dữ liệu hợp lệ (không null, không rỗng) để so sánh
        Map<Integer, CarAttribute> validEntries = carAttributeMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().getValue() != null && !entry.getValue().getValue().trim().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String ruleCode = Optional.ofNullable(attribute.getComparisonRule())
                .map(rule -> rule.getCode().toLowerCase())
                .orElse("none");

        // 2. Điều kiện thoát sớm: nếu không thể so sánh
        if (validEntries.size() < 2 || "none".equals(ruleCode)) {
            return createNonComparableResult(carAttributeMap);
        }

        // 3. Chuyển đổi giá trị sang dạng số
        Map<Integer, Double> numericValues = new HashMap<>();
        try {
            switch (ruleCode) {
                case "higher_is_better", "lower_is_better":
                    for (Map.Entry<Integer, CarAttribute> entry : validEntries.entrySet()) {
                        String rawValue = entry.getValue().getValue();
                        String primaryValue = rawValue.split("/")[0].trim();
                        String numericString = primaryValue.replaceAll("[^\\d,.-]", "").replace(',', '.');
                        if (numericString.isEmpty() || "-".equals(numericString)) continue;
                        numericValues.put(entry.getKey(), Double.parseDouble(numericString));
                    }
                    break;
                case "boolean_true_better":
                    Set<String> trueValues = Set.of("true", "có", "1", "yes", "trang bị");
                    for (Map.Entry<Integer, CarAttribute> entry : validEntries.entrySet()) {
                        boolean isTrue = trueValues.contains(entry.getValue().getValue().toLowerCase().trim());
                        numericValues.put(entry.getKey(), isTrue ? 1.0 : 0.0);
                    }
                    break;
                case "enum_order":
                    Map<String, Integer> rankCache = enumOrderRepository
                            .findById_AttributeIdOrderByRankAsc(attribute.getAttributeId()).stream()
                            .collect(Collectors.toMap(e -> e.getId().getValueKey(), AttributeEnumOrder::getRank));
                    for (Map.Entry<Integer, CarAttribute> entry : validEntries.entrySet()) {
                        String valueFromCar = entry.getValue().getValue();
                        Integer rank = rankCache.get(valueFromCar);
                        if (rank == null) throw new IllegalStateException(String.format("Missing rank! Attr: '%s', Value: '%s'", attribute.getName(), valueFromCar));
                        numericValues.put(entry.getKey(), (double) rank);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("Error comparing attribute '{}': {}. Defaulting to non-comparable.", attribute.getName(), e.getMessage());
            return createNonComparableResult(carAttributeMap);
        }

        // Kiểm tra lại sau khi parse, nếu không có đủ 2 giá trị số hợp lệ thì cũng không so sánh
        if (numericValues.size() < 2) {
            return createNonComparableResult(carAttributeMap);
        }

        // 4. So sánh và tính điểm
        Map<Integer, ComparisonOutcome> outcomeMap = new HashMap<>();
        boolean allEqual = numericValues.values().stream().distinct().count() <= 1;

        if (allEqual) {
            numericValues.keySet().forEach(carId -> outcomeMap.put(carId, ComparisonOutcome.EQUAL));
        } else {
            double bestValue = "lower_is_better".equals(ruleCode) ? Collections.min(numericValues.values()) : Collections.max(numericValues.values());
            final double epsilon = 1e-9;
            long winnerCount = numericValues.values().stream().filter(v -> Math.abs(v - bestValue) < epsilon).count();

            for (Integer carId : numericValues.keySet()) {
                if (Math.abs(numericValues.get(carId) - bestValue) < epsilon) {
                    outcomeMap.put(carId, ComparisonOutcome.WIN);
                    float weight = (attribute.getWeight() != null) ? attribute.getWeight() : 0.0f;
                    carScores.computeIfPresent(carId, (k, oldScore) -> oldScore + (weight / winnerCount));
                } else {
                    outcomeMap.put(carId, ComparisonOutcome.LOSE);
                }
            }
        }

        // 5. Xây dựng kết quả cuối cùng cho TẤT CẢ các xe
        return carAttributeMap.keySet().stream()
                .map(carId -> {
                    CarAttribute originalAttr = carAttributeMap.get(carId);
                    String displayValue = (originalAttr != null && originalAttr.getValue() != null && !originalAttr.getValue().isBlank()) ? originalAttr.getValue() : "—";
                    return new AttributeValueComparisonDTO(
                            carId,
                            displayValue,
                            outcomeMap.getOrDefault(carId, ComparisonOutcome.NOT_COMPARABLE)
                    );
                })
                .sorted(Comparator.comparing(AttributeValueComparisonDTO::carId))
                .collect(Collectors.toList());
    }

    /**
     * Helper: Tạo kết quả NOT_COMPARABLE cho tất cả các xe trong map.
     */
    private List<AttributeValueComparisonDTO> createNonComparableResult(Map<Integer, CarAttribute> carAttributeMap) {
        return carAttributeMap.keySet().stream()
                .map(carId -> {
                    CarAttribute attr = carAttributeMap.get(carId);
                    String displayValue = (attr != null && attr.getValue() != null && !attr.getValue().isBlank()) ? attr.getValue() : "—";
                    return new AttributeValueComparisonDTO(carId, displayValue, ComparisonOutcome.NOT_COMPARABLE);
                })
                .sorted(Comparator.comparing(AttributeValueComparisonDTO::carId))
                .collect(Collectors.toList());
    }

    /**
     * Helper: Nhóm kết quả so sánh theo Specification và sắp xếp theo thứ tự định sẵn.
     */
    private List<SpecificationComparisonDTO> groupComparisonsBySpecification(
            List<AttributeComparisonDTO> allAttributeComparisons, List<Attribute> allAttributes) {

        Map<String, Specification> attributeNameToSpecMap = allAttributes.stream()
                .filter(attr -> attr.getSpecification() != null)
                .collect(Collectors.toMap(Attribute::getName, Attribute::getSpecification, (spec1, spec2) -> spec1));

        List<String> specOrder = List.of("Thông tin chung", "Động cơ/hộp số", "Kích thước/trọng lượng", "Hệ thống treo/phanh", "Ngoại thất", "Nội thất", "Hỗ trợ vận hành", "Công nghệ an toàn");
        Map<String, List<AttributeComparisonDTO>> groupedBySpecName = new LinkedHashMap<>();
        specOrder.forEach(specName -> groupedBySpecName.put(specName, new ArrayList<>()));

        for (AttributeComparisonDTO attrComp : allAttributeComparisons) {
            Specification spec = attributeNameToSpecMap.get(attrComp.attributeName());
            if (spec != null) {
                groupedBySpecName.computeIfAbsent(spec.getName(), k -> new ArrayList<>()).add(attrComp);
            }
        }

        return groupedBySpecName.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> SpecificationComparisonDTO.builder()
                        .specificationName(entry.getKey())
                        .attributeComparisons(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}