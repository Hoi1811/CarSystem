package web.car_system.Car_Service.utility;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import web.car_system.Car_Service.domain.dto.global.FilterCarPaginationRequestDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarType;
import web.car_system.Car_Service.domain.entity.Origin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarSpecification {
    public static Specification<Car> withFilters(FilterCarPaginationRequestDTO filter) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Áp dụng các bộ lọc chung cho query chính
            applyCommonFilters(filter, root, cb, predicates);

            // Subquery để lấy giá rẻ nhất cho xe có cùng tên, thỏa mãn các bộ lọc
            Subquery<BigDecimal> minPriceSubquery = query.subquery(BigDecimal.class);
            Root<Car> subRoot = minPriceSubquery.from(Car.class);
            List<Predicate> subQueryPredicates = new ArrayList<>();

            // Điều kiện cơ bản của subquery: cùng tên
            subQueryPredicates.add(cb.equal(subRoot.get("name"), root.get("name")));

            // Áp dụng lại các bộ lọc cho subquery
            applyCommonFilters(filter, subRoot, cb, subQueryPredicates);

            minPriceSubquery.select(cb.min(subRoot.get("price")))
                    .where(cb.and(subQueryPredicates.toArray(new Predicate[0])));

            predicates.add(cb.equal(root.get("price"), minPriceSubquery));

            // Xử lý sắp xếp dựa trên fields và direction
            applySorting(filter, root, query, cb);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void applyCommonFilters(FilterCarPaginationRequestDTO filter, Root<Car> root,
                                           CriteriaBuilder cb, List<Predicate> predicates) {
        // Keyword
        if (filter.keyword() != null && !filter.keyword().isEmpty()) {
            String keywordPattern = "%" + filter.keyword().toLowerCase() + "%";
            Predicate carNamePredicate = cb.like(cb.lower(root.get("name")), keywordPattern);
            Predicate manufacturerNamePredicate = cb.like(cb.lower(root.get("manufacturer").get("name")), keywordPattern);
            predicates.add(cb.or(carNamePredicate, manufacturerNamePredicate));
        }

        // ManufacturerIds
        List<Integer> manufacturerIdsList = filter.getManufacturerIdsAsList();
        if (!manufacturerIdsList.isEmpty()) {
            predicates.add(root.get("manufacturer").get("id").in(manufacturerIdsList));
        }

        // CarSegmentIds
        List<Integer> carSegmentIdsList = filter.getCarSegmentIdsAsList();
        if (!carSegmentIdsList.isEmpty()) {
            predicates.add(root.get("segmentId").in(carSegmentIdsList));
        }

        // CarTypeIds
        List<Integer> carTypeIdsList = filter.getCarTypeIdsAsList();
        if (!carTypeIdsList.isEmpty()) {
            Join<Car, CarType> carTypeJoin = root.join("carTypes", JoinType.LEFT);
            predicates.add(carTypeJoin.get("typeId").in(carTypeIdsList));
        }

        // Origin
        List<Origin> originsList = filter.getOriginsAsList();
        if (!originsList.isEmpty()) {
            predicates.add(root.get("origin").in(originsList));
        }

        // MinPrice
        if (filter.minPrice() != null && filter.minPrice().compareTo(BigDecimal.ZERO) > 0) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
        }

        // MaxPrice
        if (filter.maxPrice() != null && filter.maxPrice().compareTo(BigDecimal.ZERO) > 0) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
        }
    }

    private static void applySorting(FilterCarPaginationRequestDTO filter, Root<Car> root,
                                     CriteriaQuery<?> query, CriteriaBuilder cb) {
        String fields = filter.fields();
        String direction = filter.direction();

        if (fields != null && !fields.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            String[] fieldArray = fields.split(",");
            for (String field : fieldArray) {
                field = field.trim();
                if (!field.isEmpty()) {
                    // Chỉ cho phép các trường hợp lệ để tránh lỗi SQL injection
                    if (isValidSortField(field)) {
                        if ("DESC".equalsIgnoreCase(direction)) {
                            orders.add(cb.desc(root.get(field)));
                        } else {
                            orders.add(cb.asc(root.get(field)));
                        }
                    }
                }
            }
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
        }
    }

    private static boolean isValidSortField(String field) {
        // Danh sách các trường hợp lệ để sắp xếp
        // Cập nhật theo schema của entity Car
        return List.of("name", "price", "id").contains(field);
    }
}