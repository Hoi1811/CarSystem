package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "attributes")
@Data
@Audited
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

    @Column
    private String description; // Mô tả thuộc tính

    @Column(name = "control_type")
    private String controlType; // Loại điều khiển (ví dụ: "text", "number", "select")

    @Column(name = "options_source")
    private String optionsSource; // Nguồn dữ liệu cho các tùy chọn (nếu là loại "select")

    @Column(length = 20)
    private String unit; // Đơn vị tính, ví dụ: "mm", "kg", "hp", "L/100km"

    @Column
    private Float weight; // Trọng số của thuộc tính này khi tính điểm tổng thể (ví dụ: 0.1, 0.15)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comparison_rule_id") // Liên kết đến luật so sánh
    private ComparisonRule comparisonRule;

    @Override
    public String toString() {
        return "Attribute{" +
                "attributeId=" + attributeId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
