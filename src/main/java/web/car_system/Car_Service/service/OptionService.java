package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.OptionDto;

import java.util.List;

public interface OptionService {
    List<OptionDto> getOptionsBySourceName(String sourceName);

}
