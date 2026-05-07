package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.car_system.Car_Service.domain.entity.*;

import java.util.List;

public interface CarAttributeRepository extends JpaRepository<CarAttribute, CarAttributeId> {
    CarAttribute findByCarAndAttribute(Car car, Attribute attribute);
    List<CarAttribute> findByCarCarId(Integer carId);
    boolean existsByAttribute_AttributeId(Integer attributeId);

    /**
     * Fetch toàn bộ car_attribute kèm Attribute (để lấy tên) trong 1 query —
     * dùng khi build embedding cho cả catalog, tránh N+1.
     */
    @Query("SELECT ca FROM CarAttribute ca JOIN FETCH ca.attribute")
    List<CarAttribute> findAllFetchAttribute();

    /**
     * Trả về car_id của xe **pure EV** — có "Loại pin" attribute (đã đổ pin)
     * và KHÔNG có "Loại nhiên liệu" = GASOLINE/DIESEL.
     * Dùng cho hybrid retrieval khi user query "xe điện".
     *
     * Heuristic dựa trên data thực 2026-05: ~58 xe match (VinFast VF, BYD Atto/Seal,
     * Hyundai Ioniq, MG MG4, Aion...). Loại trừ HEV/PHEV vì user nói "xe điện"
     * tiếng Việt thường nghĩa là pure EV (HEV gọi là "hybrid" hoặc "lai").
     */
    @Query("""
            SELECT DISTINCT ca.car.carId
            FROM CarAttribute ca
            WHERE ca.attribute.name LIKE 'Loại pin%'
              AND ca.car.carId NOT IN (
                  SELECT ca2.car.carId FROM CarAttribute ca2
                  WHERE ca2.attribute.name LIKE 'Loại nhiên liệu%'
                    AND ca2.value IN ('GASOLINE', 'DIESEL')
              )
            """)
    List<Integer> findPureEvCarIds();
}
