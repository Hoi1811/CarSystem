package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_drive_appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE test_drive_appointments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TestDriveAppointment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    private String email;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "preferred_datetime", nullable = false)
    private LocalDateTime preferredDateTime;

    @Column(name = "confirmed_datetime")
    private LocalDateTime confirmedDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus appointmentStatus;

    @Lob
    @Column(name = "customer_notes")
    private String customerNotes;

    @Lob
    @Column(name = "admin_notes")
    private String adminNotes;

    // ----- Các mối quan hệ -----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_car_id", nullable = false)
    private InventoryCar car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assignee;

    @PrePersist
    public void setDefaultValues() {
        // Luôn kiểm tra if null để tránh ghi đè lên giá trị đã được set thủ công
        if (this.appointmentStatus == null) {
            this.appointmentStatus = AppointmentStatus.PENDING_CONFIRMATION;
        }
    }
}