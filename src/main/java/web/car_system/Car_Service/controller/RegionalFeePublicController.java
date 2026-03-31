package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.service.RegionalFeeService;

import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.REGIONAL_FEE.GET_ALL_PUBLIC;
import static web.car_system.Car_Service.utility.ResponseFactory.success;

@RestApiV1
@RequiredArgsConstructor
public class RegionalFeePublicController {

    private final RegionalFeeService regionalFeeService;

    @GetMapping(GET_ALL_PUBLIC)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, List<RegionalFeeDto>>> getAll() {
        List<RegionalFeeDto> fees = regionalFeeService.getAllRegionalFees();
        return success(fees, "Lấy danh sách cấu hình phí vùng miền thành công.");
    }
}
