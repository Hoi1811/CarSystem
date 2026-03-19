package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.showroom.*;
import web.car_system.Car_Service.domain.entity.Showroom.ShowroomStatus;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.service.ShowroomService;

import java.util.List;

import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShowroomController {

    private final ShowroomService showroomService;

    // ============================================
    // 1. PUBLIC APIS (Customers & Guests)
    // ============================================

    @GetMapping("/public/showrooms")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<ShowroomDto>>> getPublicShowrooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ShowroomDto> result = showroomService.getAllShowrooms(keyword, ShowroomStatus.ACTIVE, pageable);
        return successPageable(result, "Lấy danh sách chi nhánh thành công.");
    }

    @GetMapping("/public/showrooms/{id}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, ShowroomDto>> getShowroomById(@PathVariable Long id) {
        ShowroomDto showroom = showroomService.getShowroomById(id);
        return success(showroom, "Lấy thông tin chi nhánh thành công.");
    }

    @GetMapping("/public/showrooms/{id}/reviews")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<ShowroomReviewDto>>> getShowroomReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShowroomReviewDto> result = showroomService.getReviewsByShowroom(id, pageable);
        return successPageable(result, "Lấy danh sách đánh giá thành công.");
    }

    // ============================================
    // 2. CUSTOMER APIS (Authenticated Customer)
    // ============================================

    @PreAuthorize("hasAnyRole('CUSTOMER', 'USER')")
    @PostMapping("/customer/showrooms/reviews")
    public ResponseEntity<ShowroomReviewDto> submitReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ShowroomReviewRequest request) {
        return ResponseEntity.ok(showroomService.submitReview(user.getUserId(), request));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'USER')")
    @GetMapping("/customer/showrooms/reviews")
    public ResponseEntity<Page<ShowroomReviewDto>> getMyReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(showroomService.getReviewsByCustomer(user.getUserId(), pageable));
    }

    // ============================================
    // 3. SALES/STAFF APIS (Tenant-Aware)
    // ============================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'SYSTEM_ADMIN')")
    @PutMapping("/sales/showroom-reviews/{id}/reply")
    public ResponseEntity<ShowroomReviewDto> replyToReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReplyReviewRequest request) {
        return ResponseEntity.ok(showroomService.replyToReview(id, user.getUserId(), request));
    }

    // ============================================
    // 4. ADMIN APIS (Global/System Admin)
    // ============================================

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @GetMapping("/admin/showrooms")
    public ResponseEntity<Page<ShowroomDto>> getAllShowroomsForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ShowroomStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(showroomService.getAllShowrooms(keyword, status, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PostMapping("/admin/showrooms")
    public ResponseEntity<ShowroomDto> createShowroom(@Valid @RequestBody CreateShowroomRequest request) {
        return ResponseEntity.ok(showroomService.createShowroom(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PutMapping("/admin/showrooms/{id}")
    public ResponseEntity<ShowroomDto> updateShowroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShowroomRequest request) {
        return ResponseEntity.ok(showroomService.updateShowroom(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PutMapping("/admin/showrooms/{id}/status")
    public ResponseEntity<Void> switchShowroomStatus(
            @PathVariable Long id,
            @RequestParam ShowroomStatus status) {
        showroomService.switchShowroomStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @DeleteMapping("/admin/showrooms/{id}")
    public ResponseEntity<Void> deleteShowroom(@PathVariable Long id) {
        showroomService.deleteShowroom(id);
        return ResponseEntity.ok().build();
    }
}
