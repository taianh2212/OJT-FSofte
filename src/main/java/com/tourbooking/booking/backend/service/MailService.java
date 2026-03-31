package com.tourbooking.booking.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com"); // 👈 BẮT BUỘC
            message.setTo(toEmail);
            message.setSubject("Verify your email - TourBooking");

            String link = "http://localhost:8080/api/v1/auth/verify?token=" + token;

            message.setText("Click to verify your email: " + link);

            mailSender.send(message);
            System.out.println("SEND MAIL SUCCESS");
        } catch (Exception e) {
            e.printStackTrace(); // 👈 QUAN TRỌNG
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com"); // 👈 BẮT BUỘC
            message.setTo(toEmail);
            message.setSubject("Password Reset - TourBooking");
            message.setText("Reset link: " + resetUrl);

            mailSender.send(message);
            System.out.println("SEND RESET MAIL SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UC48: Gửi email thông báo hủy booking tự động do chưa thanh toán
    public void sendBookingCancelledEmail(String toEmail, String customerName, Long bookingId, java.math.BigDecimal amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Booking #" + bookingId + " đã bị hủy");
            message.setText(
                "Xin chào " + customerName + ",\n\n" +
                "Booking #" + bookingId + " của bạn (tổng tiền: " + amount + " VND) " +
                "đã bị hủy tự động vì không có thanh toán trong vòng 24 giờ.\n\n" +
                "Nếu bạn vẫn muốn đặt tour, vui lòng truy cập lại website.\n\n" +
                "Trân trọng,\nĐội ngũ TourBooking"
            );
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UC50: Gửi báo cáo tháng cho admin
    public void sendMonthlyReportEmail(String toEmail, String reportContent, String monthYear) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Báo cáo tháng " + monthYear);
            message.setText(reportContent);
            mailSender.send(message);
            System.out.println("SEND MONTHLY REPORT SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
