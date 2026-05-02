package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.CarComment;
import web.car_system.Car_Service.domain.entity.CommentStatus;

import java.util.List;

public interface CarCommentRepository extends JpaRepository<CarComment, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<CarComment> findByCarCarIdAndParentIsNullAndCommentStatus(
            Integer carId, CommentStatus status, Pageable pageable);

    List<CarComment> findByParentIdAndCommentStatusOrderByCreatedAtAsc(
            Long parentId, CommentStatus status);

    @Query("SELECT c FROM CarComment c JOIN FETCH c.user " +
            "WHERE c.parent.id IN :parentIds AND c.commentStatus = :status " +
            "ORDER BY c.parent.id ASC, c.createdAt ASC")
    List<CarComment> findRepliesByParentIdsWithUser(
            @Param("parentIds") List<Long> parentIds,
            @Param("status") CommentStatus status);

    long countByCarCarIdAndParentIsNullAndCommentStatus(Integer carId, CommentStatus status);

    @Query("SELECT AVG(c.rating) FROM CarComment c WHERE c.car.carId = :carId AND c.parent IS NULL AND c.commentStatus = 'VISIBLE'")
    Double calculateAverageRatingByCarId(@Param("carId") Integer carId);

    Page<CarComment> findByCommentStatusOrderByCreatedAtDesc(CommentStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CarComment c WHERE c.car.carId = :carId AND c.parent IS NULL AND c.commentStatus = 'VISIBLE' AND c.rating = :rating")
    long countByCarIdAndRating(@Param("carId") Integer carId, @Param("rating") int rating);
}
