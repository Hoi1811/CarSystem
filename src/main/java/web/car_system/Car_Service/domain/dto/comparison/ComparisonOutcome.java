package web.car_system.Car_Service.domain.dto.comparison;

public enum ComparisonOutcome {
    WIN,        // Thắng (tốt nhất trong danh sách so sánh)
    LOSE,       // Thua (không phải tốt nhất)
    EQUAL,      // Hòa (tất cả các giá trị đều bằng nhau)
    NOT_COMPARABLE // Không so sánh (áp dụng cho rule 'none')
}