package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.showroom.*;
import web.car_system.Car_Service.domain.entity.Showroom.ShowroomStatus;

public interface ShowroomService {
    
    // ============================================
    // SHOWROOM MANAGEMENT
    // ============================================

    Page<ShowroomDto> getAllShowrooms(String keyword, ShowroomStatus status, Pageable pageable);

    ShowroomDto getShowroomById(Long id);

    ShowroomDto createShowroom(CreateShowroomRequest request);

    ShowroomDto updateShowroom(Long id, UpdateShowroomRequest request);

    void switchShowroomStatus(Long id, ShowroomStatus status);

    void deleteShowroom(Long id);

    // ============================================
    // REVIEWS MANAGEMENT
    // ============================================

    Page<ShowroomReviewDto> getReviewsByShowroom(Long showroomId, Pageable pageable);

    Page<ShowroomReviewDto> getReviewsByCustomer(Long customerId, Pageable pageable);

    ShowroomReviewDto submitReview(Long customerId, ShowroomReviewRequest request);

    ShowroomReviewDto replyToReview(Long reviewId, Long staffId, ReplyReviewRequest request);

    void deleteReview(Long reviewId);
}
