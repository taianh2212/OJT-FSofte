package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.model.dto.request.PaymentRequest;
import com.tourbooking.booking.backend.model.dto.response.PaymentResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.Payment;
import com.tourbooking.booking.backend.model.entity.PaymentLog;
import com.tourbooking.booking.backend.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.PaymentRepository;
import com.tourbooking.booking.backend.repository.PaymentLogRepository;
import com.tourbooking.booking.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourbooking.booking.backend.service.LoyaltyService;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository; // ✅ thêm
    private final LoyaltyService loyaltyService;

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        loyaltyService.addPoint(
                booking.getUser().getId(),
                booking.getTotalPrice().intValue() / 100000);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionCode(request.getTransactionCode());
        payment.setPaymentDate(LocalDateTime.now()); // ✅ thêm

        // UC18: trả góp
        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            payment.setAmount(booking.getTotalPrice());
            payment.setStatus(PaymentStatus.SUCCESS);

            // UC17
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        Payment saved = paymentRepository.save(payment);

        // 🔥 LOG PAYMENT
        PaymentLog log = new PaymentLog();
        log.setPayment(saved);
        log.setLogMessage(
                "Payment " + saved.getStatus() +
                        " | amount=" + saved.getAmount() +
                        " | method=" + saved.getPaymentMethod());

        paymentLogRepository.save(log);

        return PaymentResponse.builder()
                .paymentId(saved.getId())
                .bookingId(booking.getId())
                .amount(saved.getAmount())
                .paymentMethod(saved.getPaymentMethod())
                .status(saved.getStatus().name())
                .build();
    }
}