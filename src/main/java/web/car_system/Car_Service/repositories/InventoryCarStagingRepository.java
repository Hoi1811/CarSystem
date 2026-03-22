package web.car_system.Car_Service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;

@Repository
public interface InventoryCarStagingRepository extends JpaRepository<InventoryCarStaging, Long> {

    Page<InventoryCarStaging> findByStagingStatus(StagingStatus status, Pageable pageable);
    
    // Additional methods for duplicate detection might be needed later
}
