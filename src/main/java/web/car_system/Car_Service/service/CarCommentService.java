package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.car_comment.CarCommentDto;
import web.car_system.Car_Service.domain.dto.car_comment.CommentSummaryDto;
import web.car_system.Car_Service.domain.dto.car_comment.CreateCarCommentRequest;
import web.car_system.Car_Service.domain.dto.car_comment.UpdateCarCommentRequest;
import web.car_system.Car_Service.domain.entity.CommentStatus;

import java.util.Map;

public interface CarCommentService {

    Page<CarCommentDto> getCommentsByCar(Integer carId, Long currentUserId, Pageable pageable);

    CarCommentDto createComment(Long userId, CreateCarCommentRequest request);

    CarCommentDto updateComment(Long commentId, Long userId, UpdateCarCommentRequest request);

    void deleteComment(Long commentId, Long userId, boolean isAdmin);

    Map<String, Object> toggleLike(Long commentId, Long userId);

    CommentSummaryDto getCommentSummary(Integer carId);

    Page<CarCommentDto> getAllCommentsForAdmin(CommentStatus status, Pageable pageable);

    void updateCommentStatus(Long commentId, CommentStatus status);
}
