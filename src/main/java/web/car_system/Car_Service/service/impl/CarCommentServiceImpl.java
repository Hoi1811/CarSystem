package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.car_comment.CarCommentDto;
import web.car_system.Car_Service.domain.dto.car_comment.CommentSummaryDto;
import web.car_system.Car_Service.domain.dto.car_comment.CreateCarCommentRequest;
import web.car_system.Car_Service.domain.dto.car_comment.UpdateCarCommentRequest;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.CarCommentMapper;
import web.car_system.Car_Service.repository.*;
import web.car_system.Car_Service.service.CarCommentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarCommentServiceImpl implements CarCommentService {

    private final CarCommentRepository commentRepository;
    private final CommentLikeRepository likeRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarCommentMapper commentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CarCommentDto> getCommentsByCar(Integer carId, Long currentUserId, Pageable pageable) {
        Page<CarComment> comments = commentRepository.findByCarCarIdAndParentIsNullAndCommentStatus(
                carId, CommentStatus.VISIBLE, pageable);

        return comments.map(comment -> {
            CarCommentDto dto = commentMapper.toDto(comment);

            // Load replies
            List<CarComment> replies = commentRepository.findByParentIdAndCommentStatusOrderByCreatedAtAsc(
                    comment.getId(), CommentStatus.VISIBLE);
            List<CarCommentDto> replyDtos = replies.stream().map(reply -> {
                CarCommentDto replyDto = commentMapper.toDto(reply);
                if (currentUserId != null) {
                    replyDto.setLikedByCurrentUser(likeRepository.existsByCommentIdAndUserUserId(reply.getId(), currentUserId));
                }
                return replyDto;
            }).toList();
            dto.setReplies(replyDtos);

            // Set like status
            if (currentUserId != null) {
                dto.setLikedByCurrentUser(likeRepository.existsByCommentIdAndUserUserId(comment.getId(), currentUserId));
            }
            return dto;
        });
    }

    @Override
    @Transactional
    public CarCommentDto createComment(Long userId, CreateCarCommentRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + request.getCarId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Root comment requires rating; reply does not
        if (request.getParentId() == null && request.getRating() == null) {
            throw new IllegalArgumentException("Rating is required for a top-level comment");
        }

        CarComment.CarCommentBuilder builder = CarComment.builder()
                .car(car)
                .user(user)
                .content(sanitize(request.getContent()))
                .commentStatus(CommentStatus.VISIBLE);

        if (request.getParentId() != null) {
            // This is a reply — find the root comment to always group under root
            CarComment targetComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            // Determine the root: if targetComment itself has a parent, use that parent as root
            CarComment rootComment = targetComment.getParent() != null ? targetComment.getParent() : targetComment;
            builder.parent(rootComment);

            // If replying to a reply (not the root), store @mention info
            if (request.getReplyToCommentId() != null) {
                CarComment replyToComment = commentRepository.findById(request.getReplyToCommentId())
                        .orElseThrow(() -> new RuntimeException("Reply-to comment not found"));
                builder.replyToUserId(replyToComment.getUser().getUserId());
                builder.replyToUserName(replyToComment.getUser().getFullName());
            }
        } else {
            builder.rating(request.getRating());
        }

        CarComment saved = commentRepository.save(builder.build());

        // Recalculate car rating only for root comments
        if (request.getParentId() == null) {
            recalculateCarRating(car);
        }

        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CarCommentDto updateComment(Long commentId, Long userId, UpdateCarCommentRequest request) {
        CarComment comment = findCommentOrThrow(commentId);

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("You can only edit your own comments");
        }

        comment.setContent(sanitize(request.getContent()));
        if (comment.getParent() == null && request.getRating() != null) {
            comment.setRating(request.getRating());
        }

        CarComment saved = commentRepository.save(comment);

        if (comment.getParent() == null) {
            recalculateCarRating(comment.getCar());
        }

        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        CarComment comment = findCommentOrThrow(commentId);

        if (!isAdmin && !comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        Car car = comment.getCar();
        boolean isRoot = comment.getParent() == null;

        commentRepository.delete(comment);
        commentRepository.flush();

        if (isRoot) {
            recalculateCarRating(car);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long commentId, Long userId) {
        CarComment comment = findCommentOrThrow(commentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<CommentLike> existingLike = likeRepository.findByCommentIdAndUserUserId(commentId, userId);

        boolean nowLiked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            nowLiked = false;
        } else {
            CommentLike like = CommentLike.builder().comment(comment).user(user).build();
            likeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            nowLiked = true;
        }

        commentRepository.save(comment);
        return Map.of("liked", nowLiked, "likeCount", comment.getLikeCount());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentSummaryDto getCommentSummary(Integer carId) {
        long total = commentRepository.countByCarCarIdAndParentIsNullAndCommentStatus(carId, CommentStatus.VISIBLE);
        Double avg = commentRepository.calculateAverageRatingByCarId(carId);

        return CommentSummaryDto.builder()
                .carId(carId)
                .totalComments(total)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .ratingCount5(commentRepository.countByCarIdAndRating(carId, 5))
                .ratingCount4(commentRepository.countByCarIdAndRating(carId, 4))
                .ratingCount3(commentRepository.countByCarIdAndRating(carId, 3))
                .ratingCount2(commentRepository.countByCarIdAndRating(carId, 2))
                .ratingCount1(commentRepository.countByCarIdAndRating(carId, 1))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarCommentDto> getAllCommentsForAdmin(CommentStatus status, Pageable pageable) {
        if (status != null) {
            return commentRepository.findByCommentStatusOrderByCreatedAtDesc(status, pageable)
                    .map(commentMapper::toDto);
        }
        return commentRepository.findAll(pageable).map(commentMapper::toDto);
    }

    @Override
    @Transactional
    public void updateCommentStatus(Long commentId, CommentStatus status) {
        CarComment comment = findCommentOrThrow(commentId);
        comment.setCommentStatus(status);
        commentRepository.save(comment);
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private CarComment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    private void recalculateCarRating(Car car) {
        long totalComments = commentRepository.countByCarCarIdAndParentIsNullAndCommentStatus(
                car.getCarId(), CommentStatus.VISIBLE);
        Double avg = commentRepository.calculateAverageRatingByCarId(car.getCarId());

        car.setTotalComments((int) totalComments);
        car.setAverageCommentRating(
                avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue() : null);
        carRepository.save(car);
    }

    /**
     * Basic XSS prevention: strip HTML tags from user input.
     */
    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
