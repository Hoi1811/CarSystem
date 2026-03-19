package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.showroom.*;
import web.car_system.Car_Service.domain.entity.Showroom;
import web.car_system.Car_Service.domain.entity.Showroom.ShowroomStatus;
import web.car_system.Car_Service.domain.entity.ShowroomReview;
import web.car_system.Car_Service.domain.entity.ShowroomReview.ReviewStatus;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.ShowroomMapper;
import web.car_system.Car_Service.repositories.ShowroomRepository;
import web.car_system.Car_Service.repositories.ShowroomReviewRepository;
import web.car_system.Car_Service.repositories.UserRepository;
import web.car_system.Car_Service.service.ShowroomService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowroomServiceImpl implements ShowroomService {

    private final ShowroomRepository showroomRepository;
    private final ShowroomReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ShowroomMapper showroomMapper;

    // ============================================
    // SHOWROOM MANAGEMENT
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Page<ShowroomDto> getAllShowrooms(String keyword, ShowroomStatus status, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                return showroomRepository.findByNameContainingIgnoreCaseAndStatus(keyword, status, pageable)
                        .map(showroomMapper::toDto);
            }
            return showroomRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable)
                    .map(showroomMapper::toDto);
        } else if (status != null) {
            return showroomRepository.findByStatus(status, pageable)
                    .map(showroomMapper::toDto);
        }
        return showroomRepository.findAll(pageable)
                .map(showroomMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowroomDto getShowroomById(Long id) {
        Showroom showroom = showroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showroom not found"));
        return showroomMapper.toDto(showroom);
    }

    @Override
    @Transactional
    public ShowroomDto createShowroom(CreateShowroomRequest request) {
        if (showroomRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Showroom code already exists");
        }

        Showroom showroom = showroomMapper.toEntity(request);
        showroom.setAverageRating(BigDecimal.ZERO);
        showroom.setTotalReviews(0);

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            showroom.setManager(manager);
        }

        Showroom savedShowroom = showroomRepository.save(showroom);
        return showroomMapper.toDto(savedShowroom);
    }

    @Override
    @Transactional
    public ShowroomDto updateShowroom(Long id, UpdateShowroomRequest request) {
        Showroom showroom = showroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showroom not found"));

        showroomMapper.updateEntityFromRequest(request, showroom);

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            showroom.setManager(manager);
        }

        Showroom updatedShowroom = showroomRepository.save(showroom);
        return showroomMapper.toDto(updatedShowroom);
    }

    @Override
    @Transactional
    public void switchShowroomStatus(Long id, ShowroomStatus status) {
        Showroom showroom = showroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showroom not found"));
        showroom.setStatus(status);
        showroomRepository.save(showroom);
    }

    @Override
    @Transactional
    public void deleteShowroom(Long id) {
        Showroom showroom = showroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showroom not found"));
        showroomRepository.delete(showroom);
    }

    // ============================================
    // REVIEWS MANAGEMENT
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Page<ShowroomReviewDto> getReviewsByShowroom(Long showroomId, Pageable pageable) {
        return reviewRepository.findByShowroomIdAndStatus(showroomId, ReviewStatus.APPROVED, pageable)
                .map(showroomMapper::toReviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowroomReviewDto> getReviewsByCustomer(Long customerId, Pageable pageable) {
        return reviewRepository.findByCustomerId(customerId, pageable)
                .map(showroomMapper::toReviewDto);
    }

    @Override
    @Transactional
    public ShowroomReviewDto submitReview(Long customerId, ShowroomReviewRequest request) {
        Showroom showroom = showroomRepository.findById(request.getShowroomId())
                .orElseThrow(() -> new RuntimeException("Showroom not found"));
                
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Only allow 1 review per order if orderId is provided
        if (request.getOrderId() != null) {
            reviewRepository.findByOrderId(request.getOrderId()).ifPresent(r -> {
                throw new RuntimeException("You have already reviewed this order");
            });
        }

        ShowroomReview review = ShowroomReview.builder()
                .showroom(showroom)
                .customer(customer)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.APPROVED) // Auto-approve for simplicity
                .build();

        ShowroomReview savedReview = reviewRepository.save(review);
        recalculateShowroomRating(showroom);
        return showroomMapper.toReviewDto(savedReview);
    }

    @Override
    @Transactional
    public ShowroomReviewDto replyToReview(Long reviewId, Long staffId, ReplyReviewRequest request) {
        ShowroomReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        review.setReplyComment(request.getReplyComment());
        review.setReplyBy(staff);

        ShowroomReview updatedReview = reviewRepository.save(review);
        return showroomMapper.toReviewDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        ShowroomReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
                
        Showroom showroom = review.getShowroom();
        reviewRepository.delete(review);
        reviewRepository.flush(); // Force delete before recounting
        
        recalculateShowroomRating(showroom);
    }

    private void recalculateShowroomRating(Showroom showroom) {
        long totalReviews = reviewRepository.countApprovedReviewsByShowroomId(showroom.getId());
        if (totalReviews == 0) {
            showroom.setAverageRating(BigDecimal.ZERO);
            showroom.setTotalReviews(0);
        } else {
            Double avg = reviewRepository.calculateAverageRatingByShowroomId(showroom.getId());
            if (avg != null) {
                showroom.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
                showroom.setTotalReviews((int) totalReviews);
            }
        }
        showroomRepository.save(showroom);
    }
}
