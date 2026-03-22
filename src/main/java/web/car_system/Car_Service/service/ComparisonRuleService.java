package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.comparison.ComparisonRuleDto;
import web.car_system.Car_Service.domain.entity.ControlType;
import java.util.List;

/**
 * Interface cho các dịch vụ liên quan đến ComparisonRule.
 */
public interface ComparisonRuleService {

    /**
     * Lấy danh sách tất cả các luật so sánh có trong hệ thống.
     * @return Danh sách DTO (không expose entity).
     */
    List<ComparisonRuleDto> getAllRules();

    /**
     * Trả về danh sách rule code tương thích với một ControlType.
     * @param controlType Loại điều khiển UI.
     * @return Danh sách code (ví dụ: "higher_is_better", "none").
     */
    List<String> getCompatibleRuleCodesFor(ControlType controlType);
}