package web.car_system.Car_Service.domain.dto.showroom;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyReviewRequest {
    
    @NotBlank(message = "Reply comment is required")
    private String replyComment;
}
