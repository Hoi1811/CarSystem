package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_datetime", nullable = false)
    private LocalDateTime logDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContactChannel channel;

    @Lob
    @Column(nullable = false)
    private String notes;

    // ----- Các mối quan hệ -----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Nhân viên thực hiện

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead; // Liên kết tới Lead (nếu có)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private TestDriveAppointment appointment; // Liên kết tới Lịch hẹn (nếu có)

    @PrePersist
    protected void onPrePersist() {
        if (logDateTime == null) {
            logDateTime = LocalDateTime.now();
        }
    }
}