package web.car_system.Car_Service.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.Manufacturer;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Integer> {
    boolean existsByName(String name);
}
