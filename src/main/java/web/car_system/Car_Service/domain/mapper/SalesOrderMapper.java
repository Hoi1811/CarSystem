package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.*;
import web.car_system.Car_Service.domain.dto.sales_order.CreateOrderRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderDto;
import web.car_system.Car_Service.domain.dto.sales_order.OrderSummaryDto;
import web.car_system.Car_Service.domain.dto.sales_order.UpdateOrderRequest;
import web.car_system.Car_Service.domain.entity.SalesOrder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesOrderMapper {
    
    /**
     * Convert CreateOrderRequest to SalesOrder entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true) // Auto-generated
    @Mapping(target = "inventoryCar", ignore = true) // Set manually in service
    @Mapping(target = "salesStaff", ignore = true) // Set manually in service
    @Mapping(target = "lead", ignore = true) // Set manually in service
    @Mapping(target = "totalPrice", ignore = true) // Calculated
    @Mapping(target = "paidAmount", constant = "0")
    @Mapping(target = "remainingAmount", ignore = true) // Calculated
    @Mapping(target = "orderStatus", ignore = true) // Set to DRAFT by default
    @Mapping(target = "paymentStatus", ignore = true) // Set to UNPAID by default
    @Mapping(target = "orderDate", ignore = true) // Auto-set to today
    SalesOrder toEntity(CreateOrderRequest request);
    
    /**
     * Convert SalesOrder to full OrderDto
     */
    @Mapping(target = "inventoryCarId", source = "inventoryCar.id")
    @Mapping(target = "carName", source = "inventoryCar.car.name")
    @Mapping(target = "carModel", source = "inventoryCar.car.model")
    @Mapping(target = "carColor", source = "inventoryCar.color")
    @Mapping(target = "carVin", source = "inventoryCar.vin")
    @Mapping(target = "salesStaffId", source = "salesStaff.userId")
    @Mapping(target = "salesStaffName", source = "salesStaff.fullName")
    @Mapping(target = "leadId", source = "lead.id")
    OrderDto toDto(SalesOrder order);
    
    /**
     * Convert SalesOrder to OrderSummaryDto for list views
     */
    @Mapping(target = "carName", source = "inventoryCar.car.name")
    @Mapping(target = "carModel", source = "inventoryCar.car.model")
    @Mapping(target = "carColor", source = "inventoryCar.color")
    @Mapping(target = "salesStaffName", source = "salesStaff.fullName")
    OrderSummaryDto toSummaryDto(SalesOrder order);
    
    /**
     * Update existing SalesOrder from UpdateOrderRequest
     * Only updates non-null fields
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "customerPhone", ignore = true) // Cannot change phone
    @Mapping(target = "basePrice", ignore = true) // Cannot change base price
    @Mapping(target = "inventoryCar", ignore = true) // Cannot change car
    @Mapping(target = "lead", ignore = true)
    @Mapping(target = "totalPrice", ignore = true) // Recalculated
    @Mapping(target = "paidAmount", ignore = true) // Only via payments
    @Mapping(target = "remainingAmount", ignore = true) // Recalculated
    @Mapping(target = "paymentStatus", ignore = true) // Auto-updated based on payments
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "salesStaff", ignore = true) // Set manually
    void updateFromDto(UpdateOrderRequest request, @MappingTarget SalesOrder order);
}
