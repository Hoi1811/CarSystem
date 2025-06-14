package web.car_system.Car_Service.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.CarType;

public interface CarTypeRepository extends JpaRepository<CarType, Integer> {
    boolean existsByName(@NotBlank(message = "Tên loại xe không được để trống") @Size(max = 100, message = "Tên loại xe không vượt quá 100 ký tự") String name);
}
