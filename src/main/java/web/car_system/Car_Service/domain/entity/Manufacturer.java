package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MANUFACTURERS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Manufacturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manufacturerId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String thumbnail; // Link ảnh đại diện của hãng
}
