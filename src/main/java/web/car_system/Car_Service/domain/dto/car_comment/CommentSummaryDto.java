package web.car_system.Car_Service.domain.dto.car_comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryDto {
    private Integer carId;
    private long totalComments;
    private Double averageRating;
    private long ratingCount5;
    private long ratingCount4;
    private long ratingCount3;
    private long ratingCount2;
    private long ratingCount1;
}
