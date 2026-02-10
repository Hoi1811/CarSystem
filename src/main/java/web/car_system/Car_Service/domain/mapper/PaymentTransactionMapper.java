package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import web.car_system.Car_Service.domain.dto.payment.PaymentTransactionDto;
import web.car_system.Car_Service.domain.entity.PaymentTransaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTransactionMapper {
    
    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "orderNumber", source = "salesOrder.orderNumber")
    @Mapping(target = "receivedById", source = "receivedBy.userId")
    @Mapping(target = "receivedByName", source = "receivedBy.fullName")
    PaymentTransactionDto toDto(PaymentTransaction payment);
}
