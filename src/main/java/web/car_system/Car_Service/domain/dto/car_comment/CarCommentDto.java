package web.car_system.Car_Service.domain.dto.car_comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarCommentDto {
    private Long id;
    private Integer carId;

    private Long userId;
    private String userName;
    private String userAvatar;

    private Long parentId;

    private Long replyToUserId;
    private String replyToUserName;

    private String content;
    private Integer rating;

    private CommentStatus commentStatus;
    private Integer likeCount;
    private boolean likedByCurrentUser;

    private List<CarCommentDto> replies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
