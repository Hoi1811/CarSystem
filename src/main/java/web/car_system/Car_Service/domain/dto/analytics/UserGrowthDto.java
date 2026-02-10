package web.car_system.Car_Service.domain.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for user growth tracking
 * Used for charts showing user registration trends
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthDto {
    
    private LocalDate date;
    private Long newUsers;
    private Long totalUsers;  // Cumulative count
}
