package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attribute_enum_orders")
@Data
public class AttributeEnumOrder {

    @EmbeddedId
    private AttributeEnumOrderId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attributeId") // Ánh xạ phần attributeId của khóa chính
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Column(name = "display_value", nullable = false)
    private String displayValue; // Ví dụ: "Tự động (AT)", "Số sàn (MT)"

    @Column(name = "`rank`", nullable = false)
    private Integer rank; // Thứ hạng để so sánh, ví dụ: 1, 2, 3
}