package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "attributes")
@Data
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attributeId; // ID thuộc tính

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "specification_id", nullable = false)
    private Specification specification;

    @Column(nullable = false, unique = true)
    private String name; // Tên thuộc tính (ví dụ: "Engine")

    private String description; // Mô tả thuộc tính

    @Override
    public String toString() {
        return "Attribute{" +
                "attributeId=" + attributeId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
