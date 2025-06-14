package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.entity.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByCarCarId(Integer carId);
    @Transactional
    void deleteByUrl(String s);
}
