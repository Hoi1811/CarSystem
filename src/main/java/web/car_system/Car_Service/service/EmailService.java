package web.car_system.Car_Service.service;

public interface EmailService {

    /**
     * Gửi email đặt lại mật khẩu kèm link reset (có TTL 15 phút).
     *
     * @param toEmail   địa chỉ email người nhận
     * @param resetLink link chứa token đặt lại mật khẩu
     */
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
