package web.car_system.Car_Service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.ShowroomReview;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowroomReviewRepository extends JpaRepository<ShowroomReview, Long> {
    
    Page<ShowroomReview> findByShowroomIdAndStatus(Long showroomId, ShowroomReview.ReviewStatus status, Pageable pageable);
    
    Page<ShowroomReview> findByCustomerId(Long customerId, Pageable pageable);
    
    Optional<ShowroomReview> findByOrderId(Long orderId);
    
    @Query("SELECT COUNT(r) FROM ShowroomReview r WHERE r.showroom.id = :showroomId AND r.status = 'APPROVED'")
    long countApprovedReviewsByShowroomId(@Param("showroomId") Long showroomId);

    @Query("SELECT AVG(r.rating) FROM ShowroomReview r WHERE r.showroom.id = :showroomId AND r.status = 'APPROVED'")
    Double calculateAverageRatingByShowroomId(@Param("showroomId") Long showroomId);
}
