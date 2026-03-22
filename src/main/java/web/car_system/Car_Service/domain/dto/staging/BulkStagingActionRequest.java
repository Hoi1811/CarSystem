package web.car_system.Car_Service.domain.dto.staging;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkStagingActionRequest {
    @NotEmpty(message = "Danh sách staging IDs không được để trống")
    private List<Long> stagingIds;
}
