package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.CarSegment;
import web.car_system.Car_Service.domain.entity.CarSegmentGroup;

import java.util.List;

public interface CarSegmentGroupRepository extends JpaRepository<CarSegmentGroup, Integer> {
    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM CarSegment s WHERE s.group.id = :groupId")
    boolean isGroupInUse(@Param("groupId") Integer groupId);
}

