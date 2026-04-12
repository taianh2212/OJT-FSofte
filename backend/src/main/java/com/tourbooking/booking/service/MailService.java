package com.tourbooking.booking.service;

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
            message.setFrom("krissv659@gmail.com"); // ÃƒÂ°Ã…Â¸Ã¢â‚¬ËœÃ‹â€  BÃƒÂ¡Ã‚ÂºÃ‚Â®T BUÃƒÂ¡Ã‚Â»Ã‹Å“C
            message.setTo(toEmail);
            message.setSubject("Verify your email - TourBooking");

            String link = "http://localhost:8080/api/v1/auth/verify?token=" + token;

            message.setText("Click to verify your email: " + link);

            mailSender.send(message);
            System.out.println("SEND MAIL SUCCESS");
        } catch (Exception e) {
            e.printStackTrace(); // ÃƒÂ°Ã…Â¸Ã¢â‚¬ËœÃ‹â€  QUAN TRÃƒÂ¡Ã‚Â»Ã…â€™NG
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com"); // ÃƒÂ°Ã…Â¸Ã¢â‚¬ËœÃ‹â€  BÃƒÂ¡Ã‚ÂºÃ‚Â®T BUÃƒÂ¡Ã‚Â»Ã‹Å“C
            message.setTo(toEmail);
            message.setSubject("Password Reset - TourBooking");
            message.setText("Reset link: " + resetUrl);

            mailSender.send(message);
            System.out.println("SEND RESET MAIL SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UC48: GÃƒÂ¡Ã‚Â»Ã‚Â­i email thÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡o hÃƒÂ¡Ã‚Â»Ã‚Â§y booking tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng do chÃƒâ€ Ã‚Â°a thanh toÃƒÆ’Ã‚Â¡n
    public void sendBookingCancelledEmail(String toEmail, String customerName, Long bookingId, java.math.BigDecimal amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] Booking #" + bookingId + " Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ hÃƒÂ¡Ã‚Â»Ã‚Â§y");
            message.setText(
                "Xin chÃƒÆ’Ã‚Â o " + customerName + ",\n\n" +
                "Booking #" + bookingId + " cÃƒÂ¡Ã‚Â»Ã‚Â§a bÃƒÂ¡Ã‚ÂºÃ‚Â¡n (tÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng tiÃƒÂ¡Ã‚Â»Ã‚Ân: " + amount + " VND) " +
                "Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ hÃƒÂ¡Ã‚Â»Ã‚Â§y tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng vÃƒÆ’Ã‚Â¬ khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ thanh toÃƒÆ’Ã‚Â¡n trong vÃƒÆ’Ã‚Â²ng 24 giÃƒÂ¡Ã‚Â»Ã‚Â.\n\n" +
                "NÃƒÂ¡Ã‚ÂºÃ‚Â¿u bÃƒÂ¡Ã‚ÂºÃ‚Â¡n vÃƒÂ¡Ã‚ÂºÃ‚Â«n muÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœn Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t tour, vui lÃƒÆ’Ã‚Â²ng truy cÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i website.\n\n" +
                "TrÃƒÆ’Ã‚Â¢n trÃƒÂ¡Ã‚Â»Ã‚Âng,\nÃƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢i ngÃƒâ€¦Ã‚Â© TourBooking"
            );
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UC50: GÃƒÂ¡Ã‚Â»Ã‚Â­i bÃƒÆ’Ã‚Â¡o cÃƒÆ’Ã‚Â¡o thÃƒÆ’Ã‚Â¡ng cho admin
    public void sendMonthlyReportEmail(String toEmail, String reportContent, String monthYear) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("krissv659@gmail.com");
            message.setTo(toEmail);
            message.setSubject("[TourBooking] BÃƒÆ’Ã‚Â¡o cÃƒÆ’Ã‚Â¡o thÃƒÆ’Ã‚Â¡ng " + monthYear);
            message.setText(reportContent);
            mailSender.send(message);
            System.out.println("SEND MONTHLY REPORT SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
