package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "car_issue_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class CarIssueReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    @JsonBackReference
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Người báo cáo
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private User reporter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // Nội dung khách hàng báo sai sót

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus reportStatus; // PENDING, RESOLVED, IGNORED

    public enum ReportStatus {
        PENDING,
        RESOLVED,
        IGNORED
    }
}
