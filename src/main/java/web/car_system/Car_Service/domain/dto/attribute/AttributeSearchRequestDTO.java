package web.car_system.Car_Service.domain.dto.attribute;

import jakarta.validation.constraints.NotNull;

public record AttributeSearchRequestDTO(
        @NotNull
        Integer specificationId, // ID của Specification
        String keyword,         // Từ khóa tìm kiếm trong name của Attribute
        int page,              // Số trang (bắt đầu từ 0)
        int size               // Số lượng Attribute mỗi trang
) {
    public AttributeSearchRequestDTO {
        // Đảm bảo specificationId, page, size hợp lệ
        if (specificationId == null || specificationId <= 0) {
            throw new IllegalArgumentException("specificationId must be a positive integer");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = ""; // Mặc định tìm tất cả nếu keyword rỗng
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 15; // Mặc định 15 Attribute mỗi trang
        }
    }
}