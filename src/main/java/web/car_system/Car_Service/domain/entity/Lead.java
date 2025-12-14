package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Lead extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private LeadRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus leadStatus;

    @Lob
    private String notes;

    // ----- Các mối quan hệ -----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_car_id")
    private InventoryCar interestedCar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assignee;

}
