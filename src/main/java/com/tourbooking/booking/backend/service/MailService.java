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
}
