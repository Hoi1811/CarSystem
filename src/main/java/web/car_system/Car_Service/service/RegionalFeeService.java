package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.regional_fee.CreateRegionalFeeRequest;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.domain.dto.regional_fee.RollingCostDto;
import web.car_system.Car_Service.domain.dto.regional_fee.RollingCostRequest;
import web.car_system.Car_Service.domain.dto.regional_fee.UpdateRegionalFeeRequest;

import java.util.List;

public interface RegionalFeeService {
    // === Nghiệp vụ cho Khách hàng ===

    /**
     * Tính toán chi phí lăn bánh cho một mẫu xe dựa trên giá niêm yết
     * và khu vực đăng ký.
     * @param carId ID của mẫu xe (Car).
     * @param request Chứa thông tin tỉnh/thành phố.
     * @return DTO chứa chi tiết chi phí lăn bánh.
     */
    RollingCostDto calculateRollingCost(Integer carId, RollingCostRequest request);

    // === Nghiệp vụ CRUD cho Admin ===

    List<RegionalFeeDto> getAllRegionalFees();

    RegionalFeeDto createRegionalFee(CreateRegionalFeeRequest request);

    RegionalFeeDto updateRegionalFee(Long id, UpdateRegionalFeeRequest request);

    void deleteRegionalFee(Long id);
}
