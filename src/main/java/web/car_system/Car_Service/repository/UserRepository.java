package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.User;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);
    Optional<User> findByUsername(String username);
    Optional<User> findByExternalIdAndProvider(String externalId, String provider);
}