package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import web.car_system.Car_Service.domain.dto.regional_fee.CreateRegionalFeeRequest;
import web.car_system.Car_Service.domain.dto.regional_fee.RegionalFeeDto;
import web.car_system.Car_Service.domain.dto.regional_fee.UpdateRegionalFeeRequest;
import web.car_system.Car_Service.domain.entity.RegionalFee;

@Mapper(componentModel = "spring")
public interface RegionalFeeMapper {

    RegionalFeeDto toDto(RegionalFee regionalFee);

    RegionalFee toEntity(RegionalFeeDto regionalFeeDto);

    RegionalFee fromCreateRequest(CreateRegionalFeeRequest request);

    /** Apply only non-null fields from the update request onto an existing entity. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateRegionalFeeRequest request, @MappingTarget RegionalFee entity);
}