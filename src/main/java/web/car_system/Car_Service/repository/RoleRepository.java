package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.car_system.Car_Service.domain.entity.Role;


import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

//    @Query("select r from Role r join fetch User u where u.userId = :userId")
//    List<Role> findRolesByUserId(Long userId);

    // Lấy danh sách tên roles theo userId
    @Query("SELECT DISTINCT r.name FROM User u JOIN u.roles r WHERE u.userId = :userId")
    List<String> findRoleNamesByUserId(Long userId);

    // Lấy danh sách tên permissions theo userId
    @Query("SELECT DISTINCT p.name FROM User u JOIN u.roles r JOIN r.permissions p WHERE u.userId = :userId")
    List<String> findPermissionNamesByUserId(Long userId);

    Page<Role> findByNameContaining(String keyword, Pageable pageable);
}