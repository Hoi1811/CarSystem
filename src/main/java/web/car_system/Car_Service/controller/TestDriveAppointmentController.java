package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.test_drive.TestDriveFilterRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.CreateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.TestDriveAppointmentDto;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.UpdateTestDriveAppointmentRequest;
import web.car_system.Car_Service.service.TestDriveAppointmentService;

import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.TEST_DRIVE.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequiredArgsConstructor
public class TestDriveAppointmentController {
    private final TestDriveAppointmentService appointmentService;

    // === PUBLIC ENDPOINT ===

    @PostMapping(SUBMIT_APPOINTMENT)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, TestDriveAppointmentDto>> submitAppointment(
            @Valid @RequestBody CreateTestDriveAppointmentRequest request) {
        TestDriveAppointmentDto createdAppointment = appointmentService.createAppointment(request);
        return success(createdAppointment, "Gửi yêu cầu lái thử thành công! Chúng tôi sẽ liên hệ lại để xác nhận.", HttpStatus.CREATED);
    }

    // === ADMIN ENDPOINTS ===

    @GetMapping(GET_ALL)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<TestDriveAppointmentDto>>> getAllAppointments(Pageable pageable) {
        Page<TestDriveAppointmentDto> appointmentPage = appointmentService.getAllAppointments(pageable);
        return successPageable(appointmentPage, "Lấy danh sách lịch hẹn thành công.");
    }
    
    /**
     * Search/filter test drive appointments với các tiêu chí phức tạp
     * Sử dụng GET với query parameters
     */
    @GetMapping(SEARCH)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<TestDriveAppointmentDto>>> searchAppointments(
            @ModelAttribute TestDriveFilterRequest filter,
            Pageable pageable) {
        Page<TestDriveAppointmentDto> appointmentPage = appointmentService.searchAppointments(filter, pageable);
        return successPageable(appointmentPage, "Tìm kiếm lịch hẹn lái thử thành công.");
    }

    @GetMapping(GET_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, TestDriveAppointmentDto>> getAppointmentById(@PathVariable Long id) {
        TestDriveAppointmentDto appointment = appointmentService.getAppointmentById(id);
        return success(appointment, "Lấy chi tiết lịch hẹn thành công.");
    }

    @DeleteMapping(DELETE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, Void>> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return success(null, "Xóa lịch hẹn thành công.");
    }

    @PutMapping(UPDATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, TestDriveAppointmentDto>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestDriveAppointmentRequest request) {
        TestDriveAppointmentDto updatedAppointment = appointmentService.updateAppointment(id, request);
        return success(updatedAppointment, "Cập nhật lịch hẹn thành công.");
    }

}
