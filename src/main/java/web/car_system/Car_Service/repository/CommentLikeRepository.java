package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.entity.CommentLike;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndUserUserId(Long commentId, Long userId);

    boolean existsByCommentIdAndUserUserId(Long commentId, Long userId);

    @Query("SELECT cl.comment.id FROM CommentLike cl " +
            "WHERE cl.comment.id IN :commentIds AND cl.user.userId = :userId")
    List<Long> findLikedCommentIdsByUser(
            @Param("commentIds") List<Long> commentIds,
            @Param("userId") Long userId);
}
