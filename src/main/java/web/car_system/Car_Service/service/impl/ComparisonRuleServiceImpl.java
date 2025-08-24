package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.entity.ComparisonRule;
import web.car_system.Car_Service.repository.ComparisonRuleRepository;
import web.car_system.Car_Service.service.ComparisonRuleService;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Toàn bộ service này chỉ đọc, nên set readOnly=true để tối ưu
public class ComparisonRuleServiceImpl implements ComparisonRuleService {

    private final ComparisonRuleRepository comparisonRuleRepository;

    @Override
    public List<ComparisonRule> getAllRules() {
        // Logic nghiệp vụ ở đây (hiện tại chỉ là gọi repository)
        // Trong tương lai nếu cần lọc hay xử lý gì thêm, sẽ viết ở đây.
        return comparisonRuleRepository.findAll();
    }
}