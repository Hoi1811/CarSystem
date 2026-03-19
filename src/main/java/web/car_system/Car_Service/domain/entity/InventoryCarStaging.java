package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_cars_staging")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE inventory_cars_staging SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class InventoryCarStaging extends BaseEntity {

    public enum StagingStatus {
        PENDING_REVIEW,
        READY_TO_APPROVE,
        REJECTED,
        COMPLETED
    }

    public enum OriginType {
        IMPORTED,
        LOCALLY_ASSEMBLED,
        DOMESTIC_MANUFACTURING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Basic Information ---
    @Column(name = "manufacturer_id")
    private Long manufacturerId; // If successfully mapped

    @Column(name = "raw_manufacturer_name")
    private String rawManufacturerName; // If unmapped

    @Column(nullable = false)
    private String name; // e.g. "Camry"

    private String model; // e.g. "2.5Q"

    @Column(name = "manufacture_year")
    private Integer year;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OriginType origin;

    // --- JSON Specifications ---
    @Column(name = "raw_specifications", columnDefinition = "JSON")
    private String rawSpecifications; // Raw JSON from crawler

    @Column(name = "normalized_specifications", columnDefinition = "JSON")
    private String normalizedSpecifications; // Normalized JSON

    // --- Status & Audit ---
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StagingStatus status = StagingStatus.PENDING_REVIEW;

    @Column(columnDefinition = "TEXT")
    private String note;
}
