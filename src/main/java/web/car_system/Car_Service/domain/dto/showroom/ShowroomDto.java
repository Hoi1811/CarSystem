package web.car_system.Car_Service.domain.dto.showroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.Showroom.ShowroomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowroomDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    private Long managerId;
    private String managerName;
    
    private BigDecimal averageRating;
    private Integer totalReviews;
    private ShowroomStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
