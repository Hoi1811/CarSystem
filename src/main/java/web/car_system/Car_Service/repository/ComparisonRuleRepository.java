package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.ComparisonRule;

import java.util.Optional;

public interface ComparisonRuleRepository extends JpaRepository<ComparisonRule, Integer> {

    /**
     * Tìm kiếm quy tắc so sánh dựa trên mã định danh (code) của nó.
     * @param code Mã của quy tắc so sánh (ví dụ: "higher_is_better").
     * @return một Optional chứa ComparisonRule nếu tìm thấy.
     */
    Optional<ComparisonRule> findByCode(String code);
}
