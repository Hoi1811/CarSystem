package web.car_system.Car_Service.domain.dto.car;

public record NewThumbnailInfo(
        String type, // Có thể là "EXISTING" hoặc "NEW"
        String value
)
{

}
