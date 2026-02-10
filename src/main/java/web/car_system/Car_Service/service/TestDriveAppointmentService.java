package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.test_drive.TestDriveFilterRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.CreateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.TestDriveAppointmentDto;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.UpdateTestDriveAppointmentRequest;

public interface TestDriveAppointmentService {

    /**
     * Khách hàng gửi yêu cầu đăng ký lái thử.
     * @param request thông tin đăng ký từ khách hàng.
     * @return DTO của lịch hẹn vừa được tạo.
     */
    TestDriveAppointmentDto createAppointment(CreateTestDriveAppointmentRequest request);

    /**
     * Admin lấy danh sách tất cả các lịch hẹn (có phân trang).
     * @param pageable thông tin phân trang.
     * @return một trang chứa các lịch hẹn.
     */
    Page<TestDriveAppointmentDto> getAllAppointments(Pageable pageable);
    
    /**
     * Search/filter test drive appointments với các tiêu chí phức tạp
     * @param filter TestDriveFilterRequest chứa các tiêu chí tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page chứa các TestDrive matching filter
     */
    Page<TestDriveAppointmentDto> searchAppointments(TestDriveFilterRequest filter, Pageable pageable);

    /**
     * Admin lấy chi tiết của một lịch hẹn.
     * @param id ID của lịch hẹn.
     * @return DTO chi tiết.
     */
    TestDriveAppointmentDto getAppointmentById(Long id);

    // Chúng ta có thể cần thêm các DTO cho việc cập nhật sau
    // Nhưng hiện tại có thể dùng một phương thức chung
    /**
     * Admin cập nhật thông tin/trạng thái của một lịch hẹn.
     * @param id ID của lịch hẹn cần cập nhật.
     * @param updatedInfo DTO chứa thông tin mới (chúng ta sẽ cần tạo DTO này).
     * @return DTO của lịch hẹn sau khi cập nhật.
     */
    // TestDriveAppointmentDto updateAppointment(Long id, UpdateTestDriveAppointmentRequest updatedInfo);

    /**
     * Admin xóa một lịch hẹn.
     * @param id ID của lịch hẹn cần xóa.
     */
    void deleteAppointment(Long id);

    /**
     * Admin cập nhật thông tin/trạng thái của một lịch hẹn.
     * @param id ID của lịch hẹn cần cập nhật.
     * @param request DTO chứa thông tin mới.
     * @return DTO của lịch hẹn sau khi cập nhật.
     */
    TestDriveAppointmentDto updateAppointment(Long id, UpdateTestDriveAppointmentRequest request);
}