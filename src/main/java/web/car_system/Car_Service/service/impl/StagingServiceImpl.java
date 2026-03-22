package web.car_system.Car_Service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.staging.BulkStagingActionRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingImportRequest;
import web.car_system.Car_Service.domain.dto.staging.StagingResponseDto;
import web.car_system.Car_Service.domain.entity.Attribute;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarAttribute;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging;
import web.car_system.Car_Service.domain.entity.InventoryCarStaging.StagingStatus;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repositories.InventoryCarStagingRepository;
import web.car_system.Car_Service.repository.AttributeRepository;
import web.car_system.Car_Service.repository.CarAttributeRepository;
import web.car_system.Car_Service.service.DictionaryMappingService;
import web.car_system.Car_Service.service.DictionaryMappingService.MappingResult;
import web.car_system.Car_Service.service.StagingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StagingServiceImpl implements StagingService {

    private final InventoryCarStagingRepository stagingRepository;
    private final CarRepository carRepository;
    private final AttributeRepository attributeRepository;
    private final CarAttributeRepository carAttributeRepository;
    private final DictionaryMappingService mappingService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public int importCrawledData(List<StagingImportRequest> requests) {
        int count = 0;
        for (StagingImportRequest req : requests) {
            try {
                // Convert raw JSON Map to String for DB storage
                String rawJson = objectMapper.writeValueAsString(req.getRawSpecifications());
                
                InventoryCarStaging staging = InventoryCarStaging.builder()
                        .name(req.getName())
                        .model(req.getModel())
                        .year(req.getYear())
                        .price(req.getPrice())
                        .rawSpecifications(rawJson)
                        .stagingStatus(StagingStatus.PENDING_REVIEW)
                        .build();
                        
                stagingRepository.save(staging);
                count++;
            } catch (JsonProcessingException e) {
                log.error("Failed to parse crawler JSON data for car: {}", req.getName(), e);
            }
        }
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StagingResponseDto> getStagingDataWithValidation(StagingStatus status, Pageable pageable) {
        Page<InventoryCarStaging> rawPage = stagingRepository.findByStagingStatus(status, pageable);
        
        return rawPage.map(staging -> {
            try {
                // 1. Read JSON config
                Map<String, Object> rawSpecs = objectMapper.readValue(
                        staging.getRawSpecifications(),
                        new TypeReference<Map<String, Object>>() {}
                );

                // 2. Perform Auto-mapping Rules
                MappingResult mappingResult = mappingService.normalizeSpecifications(rawSpecs);
                
                // 3. Duplicate Detection Logic (Fingerprint Match)
                boolean isDuplicate = detectDuplicate(staging);

                // 4. Determine Warning Status dynamically
                boolean hasWarnings = mappingResult.validationFlags.values().stream()
                        .anyMatch(f -> "WARNING".equals(f.status));

                if (!hasWarnings && !isDuplicate && staging.getStagingStatus() == StagingStatus.PENDING_REVIEW) {
                    // It CAN be auto-approved logically, but we don't save to DB unless requested
                    // Just a visual cue
                }

                return StagingResponseDto.builder()
                        .id(staging.getId())
                        .name(staging.getName())
                        .model(staging.getModel())
                        .year(staging.getYear())
                        .price(staging.getPrice())
                        .origin(staging.getOrigin())
                        .rawSpecifications(rawSpecs)
                        .normalizedSpecifications(mappingResult.normalizedData)
                        .validationFlags(mappingResult.validationFlags)
                        .isDuplicateDb(isDuplicate)
                        .status(staging.getStagingStatus())
                        .note(staging.getNote())
                        .createdAt(staging.getCreatedAt() != null ? staging.getCreatedAt().toLocalDateTime() : null)
                        .build();

            } catch (JsonProcessingException e) {
                log.error("Error reading specifications JSON for staging ID: {}", staging.getId(), e);
                throw new RuntimeException("Data corruption in staging table", e);
            }
        });
    }

    @Override
    @Transactional
    public StagingResponseDto manualUpdateNormalizedData(Long stagingId, Map<String, Object> updatedNormalizedSpecs) {
        InventoryCarStaging staging = stagingRepository.findById(stagingId)
                .orElseThrow(() -> new RuntimeException("Staging record not found"));

        try {
            staging.setNormalizedSpecifications(objectMapper.writeValueAsString(updatedNormalizedSpecs));
            staging.setStagingStatus(StagingStatus.READY_TO_APPROVE);
            stagingRepository.save(staging);
            
            // Re-fetch to return exact DTO
            return getStagingDataWithValidation(StagingStatus.READY_TO_APPROVE, Pageable.unpaged())
                    .stream()
                    .filter(s -> s.getId().equals(stagingId))
                    .findFirst()
                    .orElse(null);
                    
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid JSON format for update", e);
        }
    }

    @Override
    @Transactional
    public int approveStagingCars(BulkStagingActionRequest request) {
        List<InventoryCarStaging> stagingList = stagingRepository.findAllById(request.getStagingIds());
        int count = 0;
        
        for (InventoryCarStaging staging : stagingList) {
            if (staging.getStagingStatus() == StagingStatus.COMPLETED) continue;
            
            // Logic to move Staging -> Car/InventoryCar
            try {
                Car newCar = new Car();
                newCar.setName(staging.getName());
                newCar.setModel(staging.getModel() != null ? staging.getModel() : "Default");
                newCar.setYear(staging.getYear() != null ? staging.getYear() : java.time.Year.now().getValue());
                newCar.setSegmentId(1); // Default segment
                newCar.setManufacturerId(staging.getManufacturerId() != null ? staging.getManufacturerId().intValue() : 1);
                newCar.setPrice(staging.getPrice());
                
                Car savedCar = carRepository.save(newCar);

                // EAV Mapping: Write normalized specifications to CarAttribute
                if (staging.getNormalizedSpecifications() != null) {
                    Map<String, Object> specs = objectMapper.readValue(
                        staging.getNormalizedSpecifications(),
                        new TypeReference<Map<String, Object>>() {}
                    );
                    
                    for (Map.Entry<String, Object> entry : specs.entrySet()) {
                        String attrName = entry.getKey();
                        String attrValue = String.valueOf(entry.getValue());
                        
                        Optional<Attribute> optAttribute = attributeRepository.findByName(attrName);
                        optAttribute.ifPresent(attribute -> {
                            CarAttribute carAttr = new CarAttribute();
                            carAttr.setCar(savedCar);
                            carAttr.setAttribute(attribute);
                            carAttr.setValue(attrValue);
                            carAttributeRepository.save(carAttr);
                        });
                    }
                }
                
                staging.setStagingStatus(StagingStatus.COMPLETED);
                stagingRepository.save(staging);
                count++;
            } catch (Exception e) {
                log.error("Failed to approve staging ID: {}", staging.getId(), e);
            }
        }
        return count;
    }

    @Override
    @Transactional
    public void deleteStagingCars(BulkStagingActionRequest request) {
        stagingRepository.deleteAllById(request.getStagingIds());
    }

    private boolean detectDuplicate(InventoryCarStaging staging) {
        // Find if a car exists with same name and model
        if (staging.getName() == null) return false;
        
        String model = staging.getModel() != null ? staging.getModel() : "";
        return carRepository.existsByNameAndModel(staging.getName(), model);
    }
}
