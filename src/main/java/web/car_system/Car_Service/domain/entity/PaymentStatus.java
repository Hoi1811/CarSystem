package web.car_system.Car_Service.domain.entity;

public enum PaymentStatus {
    UNPAID,         // Chưa thanh toán
    PARTIAL_PAID,   // Đã thanh toán một phần (deposit)
    FULLY_PAID,     // Đã thanh toán đủ
    REFUNDED        // Đã hoàn tiền
}
