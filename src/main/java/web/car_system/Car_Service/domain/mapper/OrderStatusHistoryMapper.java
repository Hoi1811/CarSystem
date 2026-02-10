package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import web.car_system.Car_Service.domain.dto.sales_order.OrderStatusHistoryDto;
import web.car_system.Car_Service.domain.entity.OrderStatusHistory;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderStatusHistoryMapper {
    
    @Mapping(target = "changedByName", source = "changedBy.fullName")
    OrderStatusHistoryDto toDto(OrderStatusHistory history);
}
