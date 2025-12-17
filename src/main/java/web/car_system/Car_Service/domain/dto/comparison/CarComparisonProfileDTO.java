package web.car_system.Car_Service.domain.dto.comparison;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CarComparisonProfileDTO(
        Integer carId,
        String name,
        String model,
        String thumbnail,
        BigDecimal price,
        Float totalScore
) {
    public Float getTotalScore() {
        if (this.totalScore == null) {
            return 0.0f;
        }
        // Làm tròn đến 2 chữ số thập phân
        return Math.round(this.totalScore * 100.0f) / 100.0f;
    }

    // Ghi đè phương thức gốc để Jackson sử dụng getter ở trên
    @Override
    public Float totalScore() {
        return getTotalScore();
    }
}