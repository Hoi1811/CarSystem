package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "specifications")
@Data
public class Specification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer specificationId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "specification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Attribute> attributes; // Quan hệ 1:N với Attributes

    @Override
    public String toString() {
        return "Specification{" +
                "specificationId=" + specificationId +
                ", name='" + name + '\'' +
                '}';
    }
}
