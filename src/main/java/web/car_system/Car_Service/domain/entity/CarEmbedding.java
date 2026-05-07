package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "car_embeddings", indexes = {
        @Index(name = "idx_car_embedding_car", columnList = "car_id"),
        @Index(name = "idx_car_embedding_model", columnList = "model_version")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_car_embedding_car_model", columnNames = {"car_id", "model_version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE car_embeddings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class CarEmbedding extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Lob
    @Column(name = "embedding_json", nullable = false, columnDefinition = "LONGTEXT")
    private String embeddingJson;

    @Column(name = "source_text_hash", nullable = false, length = 64)
    private String sourceTextHash;

    @Lob
    @Column(name = "source_text", nullable = false, columnDefinition = "LONGTEXT")
    private String sourceText;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "dimensions", nullable = false)
    private Integer dimensions;

    @Column(name = "embedded_at", nullable = false)
    private LocalDateTime embeddedAt;
}
