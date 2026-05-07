package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.CarEmbedding;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarEmbeddingRepository extends JpaRepository<CarEmbedding, Long> {

    Optional<CarEmbedding> findByCar_CarIdAndModelVersion(Integer carId, String modelVersion);

    @Query("SELECT e FROM CarEmbedding e JOIN FETCH e.car WHERE e.modelVersion = :modelVersion")
    List<CarEmbedding> findAllByModelVersion(@Param("modelVersion") String modelVersion);

    long countByModelVersion(String modelVersion);
}
