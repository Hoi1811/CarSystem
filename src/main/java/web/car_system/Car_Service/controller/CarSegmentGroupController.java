package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupCreateDTO;
import web.car_system.Car_Service.domain.dto.car_segment_group.CarSegmentGroupUpdateDTO;
import web.car_system.Car_Service.service.CarSegmentGroupService;

@RestApiV1
@Validated
@RequiredArgsConstructor
public class CarSegmentGroupController {
    private final CarSegmentGroupService groupService;

    @PostMapping(Endpoint.V1.CAR.CAR_SEGMENT_GROUP)
    public ResponseEntity<GlobalResponseDTO<?, ?>> createGroup(
            @Valid @RequestBody CarSegmentGroupCreateDTO createDTO) {
        return ResponseEntity.ok(groupService.createGroup(createDTO));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_SEGMENT_GROUP_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getGroupById(
            @PathVariable Integer id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping(Endpoint.V1.CAR.CAR_SEGMENT_GROUP)
    public ResponseEntity<GlobalResponseDTO<?, ?>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        if (page >= 0 && size > 0) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            return ResponseEntity.ok(groupService.getAllGroups(pageable));
        }
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @PutMapping(Endpoint.V1.CAR.CAR_SEGMENT_GROUP_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> updateGroup(
            @PathVariable Integer id,
            @Valid @RequestBody CarSegmentGroupUpdateDTO updateDTO) {
        return ResponseEntity.ok(groupService.updateGroup(id, updateDTO));
    }

    @DeleteMapping(Endpoint.V1.CAR.CAR_SEGMENT_GROUP_ID)
    public ResponseEntity<GlobalResponseDTO<?, ?>> deleteGroup(
            @PathVariable Integer id) {
        return ResponseEntity.ok(groupService.deleteGroup(id));
    }
}