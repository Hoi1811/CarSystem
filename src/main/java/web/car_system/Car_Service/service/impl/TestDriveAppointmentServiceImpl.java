package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.CreateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.TestDriveAppointmentDto;
import web.car_system.Car_Service.domain.dto.test_drive_appointment.UpdateTestDriveAppointmentRequest;
import web.car_system.Car_Service.domain.entity.*;
import web.car_system.Car_Service.domain.mapper.TestDriveAppointmentMapper;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.TestDriveAppointmentRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.TestDriveAppointmentService;

@Service
@RequiredArgsConstructor
public class TestDriveAppointmentServiceImpl implements TestDriveAppointmentService {

    private final TestDriveAppointmentRepository appointmentRepository;
    private final InventoryCarRepository inventoryCarRepository;
    private final TestDriveAppointmentMapper appointmentMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TestDriveAppointmentDto createAppointment(CreateTestDriveAppointmentRequest request) {
        // Bước 1: Tìm xe mà khách muốn lái thử
        InventoryCar car = inventoryCarRepository.findById(request.getInventoryCarId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe với ID: " + request.getInventoryCarId()));

        // (Tùy chọn) Kiểm tra xem xe có sẵn sàng để lái thử không
        if (car.getSaleStatus() != SaleStatus.AVAILABLE) {
            throw new IllegalStateException("Xe này hiện không sẵn sàng để lái thử (trạng thái: " + car.getSaleStatus() + ")");
        }

        // Bước 2: Dùng mapper để chuyển request DTO sang entity
        TestDriveAppointment newAppointment = appointmentMapper.toEntity(request);

        // Bước 3: Gán các đối tượng quan hệ và giá trị mặc định
        newAppointment.setCar(car);
        // Trạng thái PENDING_CONFIRMATION và createdAt sẽ được tự động set bởi @PrePersist

        // Bước 4: Lưu vào DB
        TestDriveAppointment savedAppointment = appointmentRepository.save(newAppointment);

        // Bước 5: Dùng mapper để trả về response DTO
        return appointmentMapper.toDto(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestDriveAppointmentDto> getAllAppointments(Pageable pageable) {
        Page<TestDriveAppointment> appointmentPage = appointmentRepository.findAll(pageable);
        return appointmentPage.map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TestDriveAppointmentDto getAppointmentById(Long id) {
        TestDriveAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch hẹn với ID: " + id));
        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Không thể xóa. Không tìm thấy lịch hẹn với ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TestDriveAppointmentDto updateAppointment(Long id, UpdateTestDriveAppointmentRequest request) {
        // Bước 1: Tìm lịch hẹn hiện có
        TestDriveAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch hẹn với ID: " + id));

        // Bước 2: Xử lý thủ công các trường phức tạp (Relationships)
        // 2.1 Xử lý Assignee (Nhân viên)
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhân viên với ID: " + request.getAssigneeId()));
            // (Giả định bạn đã sửa logic, gán cho ROLE_STAFF)
            if (assignee.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                throw new IllegalArgumentException("Chỉ có thể gán cho tài khoản Nhân viên (ADMIN or STAFF).");
            }
            appointment.setAssignee(assignee);
        }
        // (Lưu ý: Nếu request.getAssigneeId() là null, chúng ta KHÔNG làm gì cả,
        // vì chúng ta muốn giữ lại assignee cũ, trừ khi có yêu cầu gỡ bỏ rõ ràng)

        // 2.2 Xử lý Xe (InventoryCar)
        if (request.getInventoryCarId() != null) {
            // Chỉ query DB nếu ID xe thay đổi
            if (!request.getInventoryCarId().equals(appointment.getCar().getId())) {
                InventoryCar newCar = inventoryCarRepository.findById(request.getInventoryCarId())
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe với ID: " + request.getInventoryCarId()));
                appointment.setCar(newCar);
            }
        }

        // Bước 3: Dùng MapStruct để cập nhật tất cả các trường đơn giản (MA THUẬT Ở ĐÂY)
        // MapStruct sẽ tự động cập nhật các trường:
        // - status
        // - confirmedDateTime
        // - adminNotes
        // - customerName
        // - phoneNumber
        // - email
        // - age
        // - gender
        // - preferredDateTime
        // - customerNotes
        // (Và nó sẽ bỏ qua bất kỳ trường nào bị null trong request)
        appointmentMapper.updateFromDto(request, appointment);

        // Bước 4: Xử lý logic nghiệp vụ sau khi đã cập nhật
        // (Chúng ta làm điều này sau khi mapping để lấy được status mới nhất)
        if (appointment.getAppointmentStatus() == AppointmentStatus.CONFIRMED) {
            if (appointment.getConfirmedDateTime() == null) {
                throw new IllegalArgumentException("Ngày giờ xác nhận (confirmedDateTime) là bắt buộc khi chuyển trạng thái sang CONFIRMED.");
            }
        } else if (appointment.getAppointmentStatus() != AppointmentStatus.CONFIRMED) {
            // Nếu trạng thái không phải CONFIRMED, tự động dọn dẹp ngày chốt
            appointment.setConfirmedDateTime(null);
        }

        // Bước 5: Lưu và trả về
        // Không cần gọi save() nếu đã có @Transactional, nhưng gọi cũng không sao
        TestDriveAppointment updatedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(updatedAppointment);
    }
}
