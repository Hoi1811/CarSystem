package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarAttributeId implements Serializable {
    @Column(name = "car_id")
    private Integer carId;

    @Column(name = "attribute_id")
    private Integer attributeId;



}