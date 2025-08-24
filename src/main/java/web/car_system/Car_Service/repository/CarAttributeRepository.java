package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.*;

import java.util.List;

public interface CarAttributeRepository extends JpaRepository<CarAttribute, CarAttributeId> {
    CarAttribute findByCarAndAttribute(Car car, Attribute attribute);
    List<CarAttribute> findByCarCarId(Integer carId);
    boolean existsByAttribute_AttributeId(Integer attributeId);

}
