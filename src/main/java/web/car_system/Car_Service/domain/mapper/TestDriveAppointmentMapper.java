package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.*;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.CreateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.TestDriveAppointmentDto;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.UpdateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.entity.TestDriveAppointment;

@Mapper(componentModel = "spring")
public interface TestDriveAppointmentMapper {

    // --- Chuyển đổi từ Entity sang DTO ---
    @Mappings({
            // Ánh xạ trạng thái
            @Mapping(source = "appointmentStatus", target = "appointmentStatus"), // THÊM MỚI: Ánh xạ tường minh cho trường status

            // Map thông tin xe tóm tắt
            @Mapping(source = "car.id", target = "carSummary.id"),
            @Mapping(source = "car.car.name", target = "carSummary.name"), // Giả định car.car.name là đúng
            @Mapping(source = "car.vin", target = "carSummary.vin"),

            // Map thông tin người phụ trách tóm tắt
            @Mapping(source = "assignee.userId", target = "assignee.id"), // SỬA LẠI: từ assignee.id -> assignee.userId
            @Mapping(source = "assignee.fullName", target = "assignee.fullName")
    })
    TestDriveAppointmentDto toDto(TestDriveAppointment appointment);

    // --- Chuyển đổi từ Request DTO sang Entity ---
    @Mappings({
            // Bỏ qua các trường sẽ được set thủ công trong service hoặc tự động
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "confirmedDateTime", ignore = true),
            @Mapping(target = "appointmentStatus", ignore = true), // SỬA LẠI: để khớp với tên thuộc tính trong Entity
            @Mapping(target = "adminNotes", ignore = true),
            @Mapping(target = "car", ignore = true),
            @Mapping(target = "assignee", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "showroom", ignore = true)
    })
    TestDriveAppointment toEntity(CreateTestDriveAppointmentRequest request);

    /**
     * Cập nhật một entity 'appointment' đã tồn tại từ dữ liệu trong 'request'.
     *
     * @param request DTO chứa dữ liệu mới (có thể là null)
     * @param appointment Entity đã được lấy từ DB (KHÔNG ĐƯỢC NULL)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Không bao giờ cập nhật ID
    @Mapping(target = "assignee", ignore = true) // Bỏ qua, vì ta sẽ xử lý thủ công (Long -> User)
    @Mapping(target = "car", ignore = true) // Bỏ qua, vì ta sẽ xử lý thủ công (Long -> InventoryCar)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "showroom", ignore = true)
    void updateFromDto(UpdateTestDriveAppointmentRequest request, @MappingTarget TestDriveAppointment appointment);
}