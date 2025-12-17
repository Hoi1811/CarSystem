package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "comparison_rules")
@Data
@Audited
public class ComparisonRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Ví dụ: higher_is_better, lower_is_better, enum_order

    @Column(nullable = false)
    private String description; // Ví dụ: "Giá trị càng cao càng tốt", "Giá trị càng thấp càng tốt"
}