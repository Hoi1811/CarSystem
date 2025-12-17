package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.domain.entity.RegionalFee;

@Mapper(componentModel = "spring")
public interface RegionalFeeMapper {

    RegionalFeeDto toDto(RegionalFee regionalFee);

    RegionalFee toEntity(RegionalFeeDto regionalFeeDto);
}