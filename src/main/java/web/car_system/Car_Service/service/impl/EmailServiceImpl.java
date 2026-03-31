package web.car_system.Car_Service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.service.EmailService;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final Logger log = Logger.getLogger(EmailServiceImpl.class.getName());

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[CarSystem] Đặt lại mật khẩu của bạn");
            message.setText(
                    "Xin chào,\n\n" +
                    "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                    "Nhấn vào liên kết bên dưới để đặt lại mật khẩu (có hiệu lực trong 15 phút):\n\n" +
                    resetLink + "\n\n" +
                    "Nếu bạn không yêu cầu điều này, hãy bỏ qua email này - tài khoản của bạn vẫn an toàn.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ CarSystem"
            );
            mailSender.send(message);
            log.info("Password reset email sent to: " + toEmail);
        } catch (Exception e) {
            log.severe("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
            // Không ném lại exception để tránh lộ lỗi ra response
        }
    }
}
