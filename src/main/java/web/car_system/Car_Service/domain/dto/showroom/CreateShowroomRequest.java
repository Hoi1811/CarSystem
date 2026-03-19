package web.car_system.Car_Service.domain.dto.showroom;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.Showroom.ShowroomStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowroomRequest {
    
    @NotBlank(message = "Showroom name is required")
    private String name;
    
    @NotBlank(message = "Showroom code is required")
    private String code;
    
    @NotBlank(message = "Showroom address is required")
    private String address;
    
    private String phone;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    private Long managerId;
    
    @Builder.Default
    private ShowroomStatus status = ShowroomStatus.ACTIVE;
}
