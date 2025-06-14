package web.car_system.Car_Service.domain.dto.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarImagesResponseDTO {
    private Integer imageId;
    private String url;
    private String fileHash;
    private Integer carId; // Chỉ trả về carId thay vì toàn bộ Car object
}
