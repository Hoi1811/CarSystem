package web.car_system.Car_Service.domain.dto.showroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.ShowroomReview.ReviewStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowroomReviewDto {
    private Long id;
    private Long showroomId;
    private String showroomName;
    
    private Long customerId;
    private String customerName;
    private String customerAvatar;
    
    private Long orderId;
    private String orderNumber;
    
    private Integer rating;
    private String comment;
    
    private String replyComment;
    private Long replyById;
    private String replyByName;
    
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
