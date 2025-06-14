package web.car_system.Car_Service.domain.dto.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ImportCarsRequestDTO(
        @JsonProperty("files")
        List<MultipartFile> jsonFiles
) {
}
