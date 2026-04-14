package com.tourbooking.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Verify your email - TourBooking");

            String link = "http://localhost:8080/api/v1/auth/verify?token=" + token;

            message.setText("Click to verify your email: " + link);

            mailSender.send(message);
            log.info("[UC48] Send verification email to {} SUCCESS", toEmail);
        } catch (Exception e) {
            log.error("[UC48] Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Password Reset - TourBooking");
            message.setText("Reset link: " + resetUrl);

            mailSender.send(message);
            log.info("[UC48] Send reset password email to {} SUCCESS", toEmail);
        } catch (Exception e) {
            log.error("[UC48] Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    // UC48: Gui email thong bao huy booking tu dong vi chua thanh toan
    public void sendBookingCancelledEmail(String toEmail, String customerName, Long bookingId, java.math.BigDecimal amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Booking #" + bookingId + " da bi huy");
            message.setText(
                "Xin chao " + customerName + ",\n\n" +
                "Booking #" + bookingId + " cua ban (tong tien: " + String.format("%,.0f", amount) + " VND) " +
                "da bi huy tu dong vi khong co thanh toan trong vong 24 gio.\n\n" +
                "Neu ban van muon dat tour, vui long truy cap lai website.\n\n" +
                "Tran trong,\nDoi ngu TourBooking"
            );
            mailSender.send(message);
            log.info("[UC48] Send booking cancelled email to {} for booking #{}", toEmail, bookingId);
        } catch (Exception e) {
            log.error("[UC48] Failed to send booking cancelled email to {}: {}", toEmail, e.getMessage());
        }
    }

    // UC48: Gui email xac nhan booking thanh cong
    public void sendBookingConfirmedEmail(String toEmail, String customerName, Long bookingId, java.math.BigDecimal amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Xac nhan Booking #" + bookingId);
            message.setText(
                "Xin chao " + customerName + ",\n\n" +
                "Booking #" + bookingId + " cua ban da duoc xac nhan thanh cong!\n" +
                "Tong tien: " + String.format("%,.0f", amount) + " VND\n\n" +
                "Cam on ban da su dung dich vu cua TourBooking.\n\n" +
                "Tran trong,\nDoi ngu TourBooking"
            );
            mailSender.send(message);
            log.info("[UC48] Send booking confirmed email to {} for booking #{}", toEmail, bookingId);
        } catch (Exception e) {
            log.error("[UC48] Failed to send booking confirmed email to {}: {}", toEmail, e.getMessage());
        }
    }

    // UC50: Gui bao cao thang cho admin
    public void sendMonthlyReportEmail(String toEmail, String reportContent, String monthYear) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Bao cao thang " + monthYear);
            message.setText(reportContent);
            mailSender.send(message);
            log.info("[UC50] Send monthly report to {} SUCCESS", toEmail);
        } catch (Exception e) {
            log.error("[UC50] Failed to send monthly report to {}: {}", toEmail, e.getMessage());
        }
    }
}
