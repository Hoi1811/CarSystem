package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@SuperBuilder // <-- SỬA Ở ĐÂY: Dùng SuperBuilder
@NoArgsConstructor // <-- THÊM VÀO
@AllArgsConstructor // <-- THÊM VÀO
public abstract class BaseEntity implements Serializable {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntityStatus status = EntityStatus.VISIBLE;

    @PrePersist
    public void onPrePersist() {
        // Đảm bảo status luôn có giá trị mặc định nếu nó đang là null
        if (this.status == null) {
            this.status = EntityStatus.VISIBLE;
        }
    }
}