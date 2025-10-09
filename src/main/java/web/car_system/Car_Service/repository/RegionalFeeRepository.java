package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.RegionalFee;

import java.util.Optional;

public interface RegionalFeeRepository extends JpaRepository<RegionalFee, Long> {

    /**
     * Tìm kiếm bộ phí lăn bánh dựa trên tên Tỉnh/Thành phố.
     * Tên Tỉnh/Thành phố là duy nhất (unique) nên kết quả trả về là Optional.
     *
     * @param provinceCity Tên Tỉnh/Thành phố cần tìm.
     * @return một Optional chứa RegionalFee nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<RegionalFee> findByProvinceCity(String provinceCity);
}