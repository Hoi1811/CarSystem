package web.car_system.Car_Service.domain.dto.staging;

import lombok.Data;

import java.util.List;

@Data
public class BulkStagingActionRequest {
    private List<Long> stagingIds;
}
