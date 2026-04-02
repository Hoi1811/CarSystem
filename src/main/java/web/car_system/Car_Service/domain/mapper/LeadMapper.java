package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import web.car_system.Car_Service.domain.dto.lead.CreateLeadRequest;
import web.car_system.Car_Service.domain.dto.lead.LeadDto;
import web.car_system.Car_Service.domain.entity.Lead;

@Mapper(componentModel = "spring", uses = {TestDriveAppointmentMapper.class}) // uses để tái sử dụng mapper cho các DTO lồng
public interface LeadMapper {

    // --- Chuyển đổi từ Entity sang DTO ---
    @Mappings({
            // Map thông tin xe tóm tắt, source là interestedCar
            @Mapping(source = "interestedCar.id", target = "carSummary.id"),
            @Mapping(source = "interestedCar.car.name", target = "carSummary.name"),
            @Mapping(source = "interestedCar.vin", target = "carSummary.vin"),
            // Map thông tin người phụ trách
            @Mapping(source = "assignee.userId", target = "assignee.id"),
            @Mapping(source = "assignee.fullName", target = "assignee.fullName")
    })
    LeadDto toDto(Lead lead);

    // --- Chuyển đổi từ Request DTO sang Entity ---
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "leadStatus", ignore = true),
            @Mapping(target = "interestedCar", ignore = true), // Sẽ gán thủ công
            @Mapping(target = "assignee", ignore = true),     // Sẽ gán thủ công
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "showroom", ignore = true)
    })
    Lead toEntity(CreateLeadRequest request);
}