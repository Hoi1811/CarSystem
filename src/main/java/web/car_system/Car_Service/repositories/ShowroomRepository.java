package web.car_system.Car_Service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.Showroom;

import java.util.Optional;

@Repository
public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
    
    Optional<Showroom> findByCode(String code);
    
    Page<Showroom> findByStatus(Showroom.ShowroomStatus status, Pageable pageable);
    
}
