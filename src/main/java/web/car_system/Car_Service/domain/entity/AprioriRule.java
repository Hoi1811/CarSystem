package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entity storing Apriori association rules
 * Format: If user views Car A, recommend Car B with X% confidence
 */
@Entity
@Table(name = "apriori_rules", indexes = {
        @Index(name = "idx_antecedent", columnList = "antecedent_car_id,confidence"),
        @Index(name = "idx_consequent", columnList = "consequent_car_id"),
        @Index(name = "idx_confidence", columnList = "confidence DESC"),
        @Index(name = "idx_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE apriori_rules SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AprioriRule extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The "IF" part: Car that was viewed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "antecedent_car_id", nullable = false)
    private Car antecedentCar;
    
    /**
     * The "THEN" part: Car to recommend
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consequent_car_id", nullable = false)
    private Car consequentCar;
    
    /**
     * Confidence: P(consequent|antecedent)
     * Example: 0.8 means 80% of users who viewed A also viewed B
     */
    @Column(name = "confidence", nullable = false)
    private Double confidence;
    
    /**
     * Support: P(antecedent AND consequent)
     * Example: 0.05 means 5% of all sessions contained both A and B
     */
    @Column(name = "support", nullable = false)
    private Double support;
    
    /**
     * Lift: confidence / P(consequent)
     * Lift > 1.0 indicates positive correlation
     */
    @Column(name = "lift", nullable = false)
    private Double lift;
    
    /**
     * When this rule was generated
     */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
