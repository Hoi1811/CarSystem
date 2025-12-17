package web.car_system.Car_Service.domain.entity;

public enum LeadStatus {
    NEW,            // Mới nhận
    PROCESSING,     // Đang xử lý / Đã liên hệ
    SUCCESSFUL,     // Thành công (vd: đã chuyển thành hợp đồng)
    FAILED          // Thất bại (vd: khách không còn nhu cầu)
}
