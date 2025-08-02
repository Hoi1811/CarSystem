package web.car_system.Car_Service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.OptionDto;
import web.car_system.Car_Service.service.OptionService;

import java.util.List;

@RestApiV1
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService; // Dùng interface thay vì implementation

    @GetMapping(Endpoint.V1.OPTIONS.OPTIONS_BY_SOURCE_NAME) // <-- SỬ DỤNG ENDPOINT TỪ HẰNG SỐ
    public ResponseEntity<List<OptionDto>> getOptions(@PathVariable String sourceName) {
        List<OptionDto> options = optionService.getOptionsBySourceName(sourceName);
        return ResponseEntity.ok(options);
    }
}