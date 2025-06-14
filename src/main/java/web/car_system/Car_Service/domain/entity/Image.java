package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "IMAGES")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId; // ID ảnh

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car; // Xe liên kết

    @Column(name = "url")
    private String url; // Link ảnh

    @Column(name = "file_hash")
    private String fileHash; // Lưu hash của file
}
