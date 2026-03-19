package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inventory_cars", indexes = {
        @Index(name = "idx_inventorycar_vin", columnList = "vin", unique = true),
        @Index(name = "idx_inventorycar_status", columnList = "sale_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE inventory_cars SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "showroom_id = :tenantId")
public class InventoryCar extends BaseEntity{ // Có thể kế thừa BaseEntity để có deleted_at

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----- Mối quan hệ tới "bản thiết kế" -----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showroom_id")
    private Showroom showroom;

    // ----- Các thuộc tính riêng của xe trong kho -----
    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 50)
    private String color;

    @Column(length = 17, unique = true) // VIN là duy nhất
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 10)
    private CarCondition conditionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 20)
    private SaleStatus saleStatus;

    // ----- Thuộc tính chỉ dành cho xe cũ -----
    private Integer mileage; // Số km đã đi

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture; // Năm sản xuất

    @Lob // Dùng cho các đoạn text dài
    private String notes; // Ghi chú về tình trạng xe
}