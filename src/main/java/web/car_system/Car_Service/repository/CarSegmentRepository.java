package web.car_system.Car_Service.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.CarSegment;

import java.util.List;

public interface CarSegmentRepository extends JpaRepository<CarSegment, Integer> {

    boolean existsByName(String name);

    List<CarSegment> findByGroupId(Integer groupId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Car c WHERE c.carSegment.segmentId = :segmentId")
    boolean isSegmentInUse(@Param("segmentId") Integer segmentId);

    // Thêm vào CarSegmentRepository
    @Query("SELECT s.name FROM CarSegment s WHERE s.group.id = :groupId AND s.name IN :names")
    List<String> findByGroupIdAndNameIn(@Param("groupId") Integer groupId, @Param("names") List<String> names);
}
