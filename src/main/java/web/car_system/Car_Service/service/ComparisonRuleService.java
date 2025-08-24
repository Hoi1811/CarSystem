package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.entity.ComparisonRule;
import java.util.List;

/**
 * Interface cho các dịch vụ liên quan đến ComparisonRule.
 */
public interface ComparisonRuleService {

    /**
     * Lấy danh sách tất cả các luật so sánh có trong hệ thống.
     * @return Một List chứa các đối tượng ComparisonRule.
     */
    List<ComparisonRule> getAllRules();

}