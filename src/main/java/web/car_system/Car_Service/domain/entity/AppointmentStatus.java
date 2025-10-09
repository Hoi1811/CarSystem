package web.car_system.Car_Service.domain.entity;

public enum AppointmentStatus {
    PENDING_CONFIRMATION, // Chờ xác nhận
    CONFIRMED,            // Đã xác nhận
    COMPLETED,            // Đã hoàn thành
    CANCELED,             // Đã hủy
    NO_SHOW               // Khách không đến
}
