package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.RecommendationRule;

import java.util.List;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {

    List<RecommendationRule> findAllByIsActiveTrue();
    boolean existsByRuleName(String ruleName); // <-- Thêm dòng này
}