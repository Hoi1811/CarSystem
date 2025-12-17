package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.domain.dto.regional_fee.RollingCostDto;
import web.car_system.Car_Service.domain.dto.regional_fee.RollingCostRequest;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.RegionalFee;
import web.car_system.Car_Service.domain.mapper.RegionalFeeMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.RegionalFeeRepository;
import web.car_system.Car_Service.service.RegionalFeeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionalFeeServiceImpl implements RegionalFeeService {

    private final CarRepository carRepository; // Repository cho các mẫu xe (Car)
    private final RegionalFeeRepository regionalFeeRepository;
    private final RegionalFeeMapper regionalFeeMapper;

    // === NGHIỆP VỤ TÍNH TOÁN CHO KHÁCH HÀNG ===

    @Override
    @Transactional(readOnly = true)
    public RollingCostDto calculateRollingCost(Integer carId, RollingCostRequest request) {
        // Bước 1: Lấy thông tin mẫu xe để có giá niêm yết
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mẫu xe với ID: " + carId));
        BigDecimal carPrice = car.getPrice();

        // Bước 2: Lấy thông tin bộ phí theo tỉnh/thành phố
        RegionalFee feeConfig = regionalFeeRepository.findByProvinceCity(request.getProvinceCity())
                .orElseThrow(() -> new EntityNotFoundException("Không có dữ liệu phí cho khu vực: " + request.getProvinceCity()));

        // Bước 3: Thực hiện tính toán
        // Phí trước bạ = Giá xe * Tỷ lệ phí
        BigDecimal registrationFee = carPrice.multiply(feeConfig.getRegistrationFeeRate());

        // Tổng chi phí = Giá xe + (tổng các loại phí)
        BigDecimal totalCost = carPrice
                .add(registrationFee)
                .add(feeConfig.getLicensePlateFee())
                .add(feeConfig.getRoadUsageFee())
                .add(feeConfig.getInspectionFee())
                .add(feeConfig.getCivilLiabilityInsuranceFee());

        // Bước 4: Xây dựng và trả về DTO kết quả
        return RollingCostDto.builder()
                .carPrice(carPrice.setScale(0, RoundingMode.HALF_UP))
                .registrationFee(registrationFee.setScale(0, RoundingMode.HALF_UP))
                .licensePlateFee(feeConfig.getLicensePlateFee())
                .roadUsageFee(feeConfig.getRoadUsageFee())
                .inspectionFee(feeConfig.getInspectionFee())
                .civilLiabilityInsuranceFee(feeConfig.getCivilLiabilityInsuranceFee())
                .totalRollingCost(totalCost.setScale(0, RoundingMode.HALF_UP))
                .build();
    }

    // === CÁC PHƯƠNG THỨC CRUD CHO ADMIN ===

    @Override
    @Transactional(readOnly = true)
    public List<RegionalFeeDto> getAllRegionalFees() {
        return regionalFeeRepository.findAll().stream()
                .map(regionalFeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegionalFeeDto createRegionalFee(RegionalFeeDto request) {
        // Kiểm tra xem tỉnh/thành đã tồn tại chưa
        if(regionalFeeRepository.findByProvinceCity(request.getProvinceCity()).isPresent()) {
            throw new IllegalArgumentException("Đã tồn tại cấu hình phí cho khu vực: " + request.getProvinceCity());
        }

        RegionalFee newFee = regionalFeeMapper.toEntity(request);
        RegionalFee savedFee = regionalFeeRepository.save(newFee);
        return regionalFeeMapper.toDto(savedFee);
    }

    @Override
    @Transactional
    public RegionalFeeDto updateRegionalFee(Long id, RegionalFeeDto request) {
        RegionalFee existingFee = regionalFeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cấu hình phí với ID: " + id));

        // Cập nhật các trường...
        existingFee.setProvinceCity(request.getProvinceCity());
        existingFee.setRegistrationFeeRate(request.getRegistrationFeeRate());
        existingFee.setLicensePlateFee(request.getLicensePlateFee());
        existingFee.setRoadUsageFee(request.getRoadUsageFee());
        existingFee.setCivilLiabilityInsuranceFee(request.getCivilLiabilityInsuranceFee());

        // ...cập nhật các trường phí còn lại

        RegionalFee updatedFee = regionalFeeRepository.save(existingFee);
        return regionalFeeMapper.toDto(updatedFee);
    }

    @Override
    @Transactional
    public void deleteRegionalFee(Long id) {
        if (!regionalFeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Không thể xóa. Không tìm thấy cấu hình phí với ID: " + id);
        }
        regionalFeeRepository.deleteById(id);
    }
}
