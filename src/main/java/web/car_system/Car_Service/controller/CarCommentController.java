package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.car_comment.CarCommentDto;
import web.car_system.Car_Service.domain.dto.car_comment.CommentSummaryDto;
import web.car_system.Car_Service.domain.dto.car_comment.CreateCarCommentRequest;
import web.car_system.Car_Service.domain.dto.car_comment.UpdateCarCommentRequest;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.entity.CommentStatus;
import web.car_system.Car_Service.service.CarCommentService;

import java.util.List;
import java.util.Map;

import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CarCommentController {

    private final CarCommentService commentService;

    // ============================================
    // 1. PUBLIC APIs
    // ============================================

    @GetMapping("/cars/{carId}/comments")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<CarCommentDto>>> getComments(
            @PathVariable Integer carId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Long currentUserId = getCurrentUserId();
        Page<CarCommentDto> result = commentService.getCommentsByCar(carId, currentUserId, pageable);
        return successPageable(result, "Lay danh sach binh luan thanh cong.");
    }

    @GetMapping("/cars/{carId}/comments/summary")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, CommentSummaryDto>> getCommentSummary(
            @PathVariable Integer carId) {
        CommentSummaryDto summary = commentService.getCommentSummary(carId);
        return success(summary, "Lay tong hop binh luan thanh cong.");
    }

    // ============================================
    // 2. AUTHENTICATED USER APIs
    // ============================================

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cars/{carId}/comments")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, CarCommentDto>> createComment(
            @PathVariable Integer carId,
            @Valid @RequestBody CreateCarCommentRequest request) {
        request.setCarId(carId);
        CarCommentDto created = commentService.createComment(getRequiredCurrentUserId(), request);
        return success(created, "Dang binh luan thanh cong.", HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/cars/comments/{commentId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, CarCommentDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCarCommentRequest request) {
        CarCommentDto updated = commentService.updateComment(commentId, getRequiredCurrentUserId(), request);
        return success(updated, "Cap nhat binh luan thanh cong.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cars/comments/{commentId}")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteComment(
            @PathVariable Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        commentService.deleteComment(commentId, getRequiredCurrentUserId(), isAdmin);
        return success(null, "Xoa binh luan thanh cong.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cars/comments/{commentId}/like")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Map<String, Object>>> toggleLike(
            @PathVariable Long commentId) {
        Map<String, Object> result = commentService.toggleLike(commentId, getRequiredCurrentUserId());
        return success(result, "Cap nhat luot thich thanh cong.");
    }

    // ============================================
    // 3. ADMIN APIs
    // ============================================

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @GetMapping("/admin/cars/comments")
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<CarCommentDto>>> getAllCommentsForAdmin(
            @RequestParam(required = false) CommentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CarCommentDto> result = commentService.getAllCommentsForAdmin(status, pageable);
        return successPageable(result, "Lay danh sach binh luan thanh cong.");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @PatchMapping("/admin/cars/comments/{commentId}/status")
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> updateCommentStatus(
            @PathVariable Long commentId,
            @RequestParam CommentStatus status) {
        commentService.updateCommentStatus(commentId, status);
        return success(null, "Cap nhat trang thai binh luan thanh cong.");
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                return Long.parseLong(auth.getName());
            }
        } catch (NumberFormatException ignored) { }
        return null;
    }

    private Long getRequiredCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) throw new IllegalStateException("No authenticated user in security context");
        return userId;
    }
}