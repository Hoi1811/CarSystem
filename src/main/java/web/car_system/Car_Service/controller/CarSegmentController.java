package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentBatchCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment.CarSegmentUpdateDTO;
import web.car_system.Car_Service.service.CarSegmentService;

@RestApiV1
@Validated
@RequiredArgsConstructor
public class CarSegmentController {
    private final CarSegmentService segmentService;

    @PostMapping(Endpoint.V1.CAR.CAR_SEGMENT)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createSegment(
            @Valid @RequestBody CarSegmentCreateDTO createDTO) {
        return ResponseEntity.ok(segmentService.createSegment(createDTO));
    }

    @PostMapping(Endpoint.V1.CAR.CAR_SEGMENT_BATCH)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createBatchSegments(
            @Valid @RequestBody CarSegmentBatchCreateDTO batchCreateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(segmentService.createBatchSegments(batchCreateDTO));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_SEGMENT_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getSegmentById(
            @PathVariable Integer segmentId) {
        return ResponseEntity.ok(segmentService.getSegmentById(segmentId));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_SEGMENT)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllSegments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        if (page >= 0 && size > 0) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            return ResponseEntity.ok(segmentService.getAllSegments(pageable));
        }
        return ResponseEntity.ok(segmentService.getAllSegments());
    }

    @GetMapping(Endpoint.V1.CAR.CAR_SEGMENT_BY_GROUP)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getSegmentsByGroup(
            @PathVariable Integer groupId) {
        return ResponseEntity.ok(segmentService.getSegmentsByGroup(groupId));
    }

    @PutMapping(Endpoint.V1.CAR.CAR_SEGMENT_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> updateSegment(
            @PathVariable Integer segmentId,
            @Valid @RequestBody CarSegmentUpdateDTO updateDTO) {
        return ResponseEntity.ok(segmentService.updateSegment(segmentId, updateDTO));
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_SEGMENT_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> deleteSegment(
            @PathVariable Integer segmentId) {
        return ResponseEntity.ok(segmentService.deleteSegment(segmentId));
    }
}