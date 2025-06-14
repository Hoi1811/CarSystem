package web.car_system.Car_Service.domain.dto.attribute;

public record AttributePaginationRequestDTO(Integer specificationId, // ID của Specification
                                            int page,               // Số trang (bắt đầu từ 0)
                                            int size               // Số lượng Attribute mỗi trang
) {
    public AttributePaginationRequestDTO {
        // Đảm bảo specificationId, page và size hợp lệ
        if (specificationId == null || specificationId <= 0) {
            throw new IllegalArgumentException("specificationId must be a positive integer");
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 15; // Mặc định 15 Attribute mỗi trang
        }
    }
}