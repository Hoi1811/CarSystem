package web.car_system.Car_Service.domain.entity;

public enum OrderStatus {
    DRAFT,                    // Đơn nháp, chưa hoàn thiện
    PENDING_DEPOSIT,          // Chờ đặt cọc
    DEPOSIT_PAID,             // Đã đặt cọc
    PENDING_FINAL_PAYMENT,    // Chờ thanh toán đủ
    PAID,                     // Đã thanh toán đủ
    PREPARING_DELIVERY,       // Đang chuẩn bị giao xe
    DELIVERED,                // Đã giao xe
    COMPLETED,                // Hoàn tất (đã ký nhận)
    CANCELLED,                // Đã hủy
    REFUND_PROCESSING         // Đang xử lý hoàn tiền
}
