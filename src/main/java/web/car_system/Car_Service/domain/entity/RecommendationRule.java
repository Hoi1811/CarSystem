package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;

    @Column(length = 500)
    private String description;

    @Lob // Đảm bảo trường này được map tới kiểu TEXT/CLOB
    @Column(name = "conditions_json", nullable = false, columnDefinition = "TEXT")
    private String conditionsJson;

    @Lob
    @Column(name = "suggestion_json", nullable = false, columnDefinition = "TEXT")
    private String suggestionJson;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Giá trị mặc định là true
}