package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "car_comments")
@AttributeOverride(name = "status", column = @Column(name = "entity_status"))
@SQLDelete(sql = "UPDATE car_comments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CarComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CarComment parent;

    @Column(name = "reply_to_user_id")
    private Long replyToUserId;

    @Column(name = "reply_to_user_name")
    private String replyToUserName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_status", nullable = false)
    @Builder.Default
    private CommentStatus commentStatus = CommentStatus.VISIBLE;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;
}
