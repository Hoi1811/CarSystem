package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.dto.projection.OptionProjection;
import web.car_system.Car_Service.domain.entity.Car;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Integer>, JpaSpecificationExecutor<Car>  {

    boolean existsByNameAndModel(String name, String model);

    Optional<Car> findByNameAndModel(String name, String model);

    // Sử dụng @EntityGraph để định nghĩa việc fetch
    @EntityGraph(attributePaths = {"carAttributes", "carAttributes.attribute"})
    Optional<Car> findById(Integer id);

    @Query("SELECT c FROM Car c " +
            "LEFT JOIN FETCH c.carAttributes ca " +
            "LEFT JOIN FETCH ca.attribute " +
            "WHERE c.carId = :id")
    Optional<Car> findByIdWithDetails(@Param("id") Integer id);

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

    @Query(value = """
    SELECT DISTINCT c
    FROM Car c
    WHERE c.segmentId = :segmentId 
      AND c.price BETWEEN :minPrice AND :maxPrice
      AND c.carId != :currentCarId
      AND c.price = (
          SELECT MIN(c2.price)
          FROM Car c2
          WHERE c2.name = c.name
            AND c2.segmentId = :segmentId 
            AND c2.price BETWEEN :minPrice AND :maxPrice
            AND c2.carId != :currentCarId
      )
    ORDER BY function('ABS', c.price - :originalPrice) ASC 
""")
    List<Car> findSimilarCars(
            @Param("segmentId") Integer segmentId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("originalPrice") BigDecimal originalPrice,
            @Param("currentCarId") Integer currentCarId,
            Pageable pageable
    );
}
