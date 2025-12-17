package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.RecommendationRule;

import java.util.List;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {

    /**
     * Lấy ra danh sách tất cả các quy tắc gợi ý đang được kích hoạt (active).
     * Logic của hệ thống gợi ý sẽ lặp qua danh sách này để tìm ra quy tắc phù hợp.
     *
     * @return một List các RecommendationRule đang hoạt động.
     */
    List<RecommendationRule> findAllByIsActiveTrue();
    boolean existsByRuleName(String ruleName); // <-- Thêm dòng này
}