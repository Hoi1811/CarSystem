package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_types")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId; // ID loại xe

    @Column(nullable = false, unique = true)
    private String name; // Tên loại xe (ví dụ: "Hybrid")

    private String description; // Mô tả loại xe

    private String thumbnail; // Link ảnh đại diện của loại xe
}
