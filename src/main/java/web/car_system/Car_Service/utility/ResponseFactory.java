package web.car_system.Car_Service.utility;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import web.car_system.Car_Service.domain.dto.global.*;

import java.util.List;


public final class ResponseFactory {

    // private constructor để ngăn việc tạo instance của lớp util này
    private ResponseFactory() {}

    // === PHƯƠNG THỨC CHO RESPONSE THÀNH CÔNG (KHÔNG PHÂN TRANG) ===

    public static <T> ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    public static <T> ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, T>> success(T data, String message, HttpStatus status) {
        NoPaginatedMeta meta = NoPaginatedMeta.builder()
                .status(Status.SUCCESS)
                .message(message)
                .build();
        GlobalResponseDTO<NoPaginatedMeta, T> response = GlobalResponseDTO.<NoPaginatedMeta, T>builder()
                .meta(meta)
                .data(data)
                .build();
        return new ResponseEntity<>(response, status);
    }

    // === PHƯƠNG THỨC CHO RESPONSE THÀNH CÔNG (CÓ PHÂN TRANG) ===

    public static <T> ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<T>>> success(Page<T> pageData, String message) {
        // 1. Tạo đối tượng Pagination (giữ nguyên)
        Pagination pagination = Pagination.builder()
                .pageIndex(pageData.getNumber())
                .pageSize((short) pageData.getSize())
                .totalItems(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();

        // 2. Tạo đối tượng Meta (giữ nguyên)
        PaginatedMeta meta = PaginatedMeta.builder()
                .status(Status.SUCCESS)
                .message(message)
                .pagination(pagination)
                .build();

        // 3. TẠO RESPONSE - THAY ĐỔI Ở ĐÂY
        GlobalResponseDTO<PaginatedMeta, List<T>> response = GlobalResponseDTO.<PaginatedMeta, List<T>>builder()
                .meta(meta)
                .data(pageData.getContent())
                .build();

        return ResponseEntity.ok(response);
    }
}