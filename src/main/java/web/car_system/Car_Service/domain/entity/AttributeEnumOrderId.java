package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Lớp khóa chính kết hợp
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeEnumOrderId implements Serializable {
    @Column(name = "attribute_id")
    private Integer attributeId;

    @Column(name = "value_key", length = 50)
    private String valueKey; // Ví dụ: "AT", "MT", "CVT"
}
