package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "carId")
@SQLDelete(sql = "UPDATE cars SET deleted_at = CURRENT_TIMESTAMP WHERE car_id = ?")
@Audited
public class Car extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id") // Nên đặt tên cột rõ ràng
    private Integer carId;

    // ID thuần để insert/update dễ dàng
    @Column(name = "segment_id", nullable = false)
    private Integer segmentId;

    @Column(name = "manufacturer_id", nullable = false)
    private Integer manufacturerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private BigDecimal price;

    private String thumbnail;

    // Liên kết đến Segment entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", insertable = false, updatable = false)
    private CarSegment carSegment;

    // Liên kết đến Manufacturer entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", insertable = false, updatable = false)
    private Manufacturer manufacturer;

    // Nhiều loại xe cho 1 xe
    @ManyToMany
    @JsonManagedReference
    @JoinTable(
            name = "CAR_CAR_TYPES",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<CarType> carTypes;

    @Enumerated(EnumType.STRING) // Lưu tên Enum dưới dạng TEXT trong DB (vd: "IMPORTED")
    @Column(name = "origin")
    private Origin origin;
    // Ảnh xe
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Image> images;

    // Thuộc tính mở rộng của xe
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CarAttribute> carAttributes;
}
