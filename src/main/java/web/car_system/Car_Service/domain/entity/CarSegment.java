package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_segments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CarSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer segmentId;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private CarSegmentGroup group;
}