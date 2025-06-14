package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.Attribute;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, Integer> {
    Optional<Attribute> findByNameAndSpecificationSpecificationId(String name, Integer specificationId);


}
