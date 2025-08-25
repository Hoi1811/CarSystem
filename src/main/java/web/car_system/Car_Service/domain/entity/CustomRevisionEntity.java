package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@RevisionEntity(CustomRevisionListener.class) // Liên kết với Listener
@Data
// Thêm 2 annotation @AttributeOverride để chỉ định lại tên cột
// nếu không Hibernate sẽ tạo cột rev và revtstmp
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "revision_id")),
        @AttributeOverride(name = "timestamp", column = @Column(name = "revision_timestamp"))
})
public class CustomRevisionEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revision_seq")
    @SequenceGenerator(name = "revision_seq", sequenceName = "revision_sequence", allocationSize = 1)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private long timestamp;
    // Thêm trường để lưu tên người dùng
    private String username;
}