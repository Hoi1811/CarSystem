package web.car_system.Car_Service.domain.dto.staging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.OriginType;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;
import web.car_system.Car_Service.service.DictionaryMappingService.ValidationFlag;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingResponseDto {
    private Long id;
    private Long manufacturerId;
    private String rawManufacturerName;
    private String name;
    private String model;
    private Integer year;
    private BigDecimal price;
    private OriginType origin;
    
    // Using String to send JSON back, or Map depends on frontend needs
    private Map<String, Object> rawSpecifications;
    private Map<String, Object> normalizedSpecifications;
    
    private StagingStatus status;
    private String note;
    private LocalDateTime createdAt;
    
    // Dynamic flags added during fetch
    private Map<String, ValidationFlag> validationFlags;
    private boolean isDuplicateDb;
}
