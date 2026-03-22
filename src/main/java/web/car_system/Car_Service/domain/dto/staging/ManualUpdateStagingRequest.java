package web.car_system.Car_Service.domain.dto.staging;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

/**
 * Typed request DTO for manually updating normalized specifications of a staging record.
 * Replaces the previous {@code Map<String, Object>} body to provide clear API contract.
 */
@Data
public class ManualUpdateStagingRequest {

    @NotNull(message = "normalizedSpecifications không được null")
    private Map<String, Object> normalizedSpecifications;
}
