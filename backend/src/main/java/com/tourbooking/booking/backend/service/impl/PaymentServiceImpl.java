package com.tourbooking.booking.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.model.dto.request.PaymentRequest;
import com.tourbooking.booking.backend.model.dto.response.PaymentResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.Payment;
import com.tourbooking.booking.backend.model.entity.PaymentLog;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import com.tourbooking.booking.backend.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.PaymentLogRepository;
import com.tourbooking.booking.backend.repository.PaymentRepository;
import com.tourbooking.booking.backend.service.LoyaltyService;
import com.tourbooking.booking.backend.service.MailService;
import com.tourbooking.booking.backend.service.PayOSService;
import com.tourbooking.booking.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final LoyaltyService loyaltyService;
    private final PayOSService payOSService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionCode(request.getTransactionCode());
        payment.setPaymentDate(LocalDateTime.now());

        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            payment.setAmount(booking.getTotalPrice());
            payment.setStatus(PaymentStatus.SUCCESS);
            booking.setStatus(BookingStatus.CONFIRMED);
            awardLoyaltyAndSendMail(booking, payment.getAmount());
        }

        Payment saved = paymentRepository.save(payment);
        bookingRepository.save(booking);
        savePaymentLog(saved, "Manual payment created");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse createPayOSPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BigDecimal payAmount = request.getAmount() != null ? request.getAmount() : booking.getTotalPrice();
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        long orderCode = System.currentTimeMillis();
        String transactionCode = "PAYOS-" + orderCode;
        int amount = payAmount.intValue();

        String checkoutUrl = payOSService.createPaymentLink(
                orderCode,
                amount,
                "Booking #" + booking.getId()
        );

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(payAmount);
        payment.setPaymentMethod("PAYOS");
        payment.setTransactionCode(transactionCode);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        savePaymentLog(saved, "PayOS link created");

        return PaymentResponse.builder()
                .paymentId(saved.getId())
                .bookingId(booking.getId())
                .amount(saved.getAmount())
                .paymentMethod(saved.getPaymentMethod())
                .status(saved.getStatus().name())
                .checkoutUrl(checkoutUrl)
                .orderCode(orderCode)
                .build();
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(String rawPayload, String signature) {
        if (!payOSService.verifyWebhookSignature(rawPayload, signature)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            JsonNode data = root.path("data");
            long orderCode = data.path("orderCode").asLong();
            String status = data.path("status").asText();
            String transactionCode = "PAYOS-" + orderCode;

            Payment payment = paymentRepository.findByTransactionCode(transactionCode)
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

            if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    payment.setPaymentDate(LocalDateTime.now());
                    Booking booking = payment.getBooking();
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                    paymentRepository.save(payment);
                    awardLoyaltyAndSendMail(booking, payment.getAmount());
                    savePaymentLog(payment, "PayOS webhook success");
                }
                return;
            }

            if ("CANCELLED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                savePaymentLog(payment, "PayOS webhook failed/cancelled");
            }
        } catch (AppException ex) {
            throw ex;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private PaymentResponse toResponse(Payment saved) {
        return PaymentResponse.builder()
                .paymentId(saved.getId())
                .bookingId(saved.getBooking().getId())
                .amount(saved.getAmount())
                .paymentMethod(saved.getPaymentMethod())
                .status(saved.getStatus().name())
                .build();
    }

    private void savePaymentLog(Payment payment, String message) {
        PaymentLog log = new PaymentLog();
        log.setPayment(payment);
        log.setLogMessage(message + " | amount=" + payment.getAmount() + " | method=" + payment.getPaymentMethod());
        paymentLogRepository.save(log);
    }

    private void awardLoyaltyAndSendMail(Booking booking, BigDecimal paidAmount) {
        if (booking.getUser() != null && paidAmount != null) {
            loyaltyService.addPoint(booking.getUser().getId(), paidAmount.intValue() / 100000);
            mailService.sendPaymentSuccessEmail(
                    booking.getUser().getEmail(),
                    booking.getUser().getFullName(),
                    booking.getId(),
                    paidAmount
            );
        }
    }
}