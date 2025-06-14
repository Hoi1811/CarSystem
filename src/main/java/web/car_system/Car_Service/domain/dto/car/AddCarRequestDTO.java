package web.car_system.Car_Service.domain.dto.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;
import web.car_system.Car_Service.domain.dto.specification.AddSpecificationRequestDTO;
import web.car_system.Car_Service.domain.entity.Origin;

import java.math.BigDecimal;
import java.util.List;

public record AddCarRequestDTO(
        @JsonProperty("manufacturer_id")
        Integer manufacturerId,
        @JsonProperty("segment_id")
        Integer segmentId,
        String name,
        String model,
        Integer year,
        BigDecimal price,
        List<MultipartFile> images,      // Danh sách ảnh
        Integer thumbnailIndex,// Chỉ số ảnh làm thumbnail (0-based)
        @JsonProperty("car_type_ids")
        List<Integer> carTypeIds,
        Origin origin,
        List<AddSpecificationRequestDTO> specifications
) {

}
