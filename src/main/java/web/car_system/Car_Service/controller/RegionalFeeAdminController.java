package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.service.RegionalFeeService;
import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.REGIONAL_FEE.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;


@RestController
@RequiredArgsConstructor
public class RegionalFeeAdminController {

    private final RegionalFeeService regionalFeeService;

    @GetMapping(GET_ALL)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<RegionalFeeDto>>> getAll() {
        List<RegionalFeeDto> fees = regionalFeeService.getAllRegionalFees();
        return success(fees, "Lấy danh sách cấu hình phí thành công.");
    }

    @PostMapping(CREATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RegionalFeeDto>> create(
            @Valid @RequestBody RegionalFeeDto request) {
        RegionalFeeDto createdFee = regionalFeeService.createRegionalFee(request);
        return success(createdFee, "Tạo cấu hình phí mới thành công.", HttpStatus.CREATED);
    }

    @PutMapping(UPDATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, RegionalFeeDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody RegionalFeeDto request) {
        RegionalFeeDto updatedFee = regionalFeeService.updateRegionalFee(id, request);
        return success(updatedFee, "Cập nhật cấu hình phí thành công.");
    }

    @DeleteMapping(DELETE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> delete(@PathVariable Long id) {
        regionalFeeService.deleteRegionalFee(id);
        return success(null, "Xóa cấu hình phí thành công.");
    }
}