package web.car_system.Car_Service.domain.dto.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarImageStatusDTO {
    private Integer carId;
    private String name;
    private String model;
    private Integer year;
    private String manufacturerName;
    private String thumbnail;
    private Long imageCount;

    public CarImageStatusDTO(
            Integer carId,
            String name,
            String model,
            Integer year,
            String manufacturerName,
            String thumbnail,
            Long imageCount
    ) {
        this.carId = carId;
        this.name = name;
        this.model = model;
        this.year = year;
        this.manufacturerName = manufacturerName;
        this.thumbnail = thumbnail;
        this.imageCount = imageCount == null ? 0L : imageCount;
    }

    public boolean isHasThumbnail() {
        return thumbnail != null && !thumbnail.isBlank();
    }
}
