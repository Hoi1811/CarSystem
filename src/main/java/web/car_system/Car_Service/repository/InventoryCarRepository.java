package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.SaleStatus;

import java.util.Optional;

public interface InventoryCarRepository extends JpaRepository<InventoryCar, Long>, JpaSpecificationExecutor<InventoryCar>{
    // Tìm một chiếc xe cụ thể bằng số VIN (rất hữu ích)
    Optional<InventoryCar> findByVin(String vin);

    // Tìm các xe theo trạng thái bán hàng (có phân trang) — eager fetch car + manufacturer + showroom
    @EntityGraph(attributePaths = {"car", "car.manufacturer", "showroom"})
    Page<InventoryCar> findAllBySaleStatus(SaleStatus saleStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"car", "car.manufacturer", "showroom"})
    Page<InventoryCar> findAllByCar_CarIdAndSaleStatus(Integer carId, SaleStatus saleStatus, Pageable pageable);

    // Filter by showroom
    @EntityGraph(attributePaths = {"car", "car.manufacturer", "showroom"})
    Page<InventoryCar> findAllBySaleStatusAndShowroomId(SaleStatus saleStatus, Long showroomId, Pageable pageable);

    @EntityGraph(attributePaths = {"car", "car.manufacturer", "showroom"})
    Page<InventoryCar> findAllByCar_CarIdAndSaleStatusAndShowroomId(Integer carId, SaleStatus saleStatus, Long showroomId, Pageable pageable);

    // Override mặc định để admin list cũng eager load
    @Override
    @EntityGraph(attributePaths = {"car", "car.manufacturer", "showroom"})
    Page<InventoryCar> findAll(Pageable pageable);

    // Count available cars by showroom
    long countBySaleStatusAndShowroomId(SaleStatus saleStatus, Long showroomId);
}
