package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import web.car_system.Car_Service.domain.entity.Permission;


import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    Page<Permission> findByNameContaining(String keyword, Pageable pageable);
}