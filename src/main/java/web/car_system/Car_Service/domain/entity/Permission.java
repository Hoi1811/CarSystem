package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE roles SET deleted_at = CURRENT_TIMESTAMP WHERE role_id = ?")
public class Permission  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(nullable = false, unique = true)
    private String name; // "user.delete", "invoice.create"

    private String description;
}