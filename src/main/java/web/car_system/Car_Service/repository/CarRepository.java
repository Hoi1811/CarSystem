package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.dto.projection.OptionProjection;
import web.car_system.Car_Service.domain.entity.Car;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Integer>, JpaSpecificationExecutor<Car> {

    List<Car> findAllByName(String name);

    @Query(value = "SELECT DISTINCT car_id AS value, " +
            "CONCAT(model, ' - ', price, ' đ') AS label " +
            "FROM cars " +
            "WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))",
            nativeQuery = true)
    List<OptionProjection> findRelatedCarsByNameNative(@Param("name") String name);

    @Query(value =
            "SELECT DISTINCT model AS value, " +
            "CONCAT(model) AS label " +
            "FROM cars " +
            "WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))",
            nativeQuery = true)
    List<OptionProjection> findRelatedModelsByCarName(String name);
    @Query(value =
            "SELECT MIN(car_id) AS value, " +
                    "name AS label " +
                    "FROM cars " +
                    "WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%')) " +
                    "GROUP BY name",
            nativeQuery = true)
    List<OptionProjection> findRelatedCarNamesByCarName(String name);
}
