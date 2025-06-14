package web.car_system.Car_Service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "car_attribute")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CarAttribute {

    @EmbeddedId
    private CarAttributeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("carId")
    @JsonBackReference
    @JoinColumn(name = "car_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Car car;

    @ManyToOne
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Column(name = "value")
    private String value;

    public CarAttribute() {
        this.id = new CarAttributeId();
    }

    @Override
    public String toString() {
        return "CarAttribute{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", value='" + value + '\'' +
                '}';
    }
}