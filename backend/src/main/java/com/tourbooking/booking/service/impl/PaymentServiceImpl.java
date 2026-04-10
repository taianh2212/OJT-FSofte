package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.exception.AppException;
import com.tourbooking.booking.exception.ErrorCode;
import com.tourbooking.booking.model.dto.request.PaymentRequest;
import com.tourbooking.booking.model.dto.response.PaymentResponse;
import com.tourbooking.booking.model.entity.Booking;
import com.tourbooking.booking.model.entity.Payment;
import com.tourbooking.booking.model.entity.PaymentLog;
import com.tourbooking.booking.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.model.entity.enums.BookingStatus;
import com.tourbooking.booking.repository.BookingRepository;
import com.tourbooking.booking.repository.PaymentRepository;
import com.tourbooking.booking.repository.PaymentLogRepository;
import com.tourbooking.booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourbooking.booking.service.LoyaltyService;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository; // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ thÃƒÆ’Ã‚Âªm
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
        payment.setPaymentDate(LocalDateTime.now()); // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ thÃƒÆ’Ã‚Âªm

        // UC18: trÃƒÂ¡Ã‚ÂºÃ‚Â£ gÃƒÆ’Ã‚Â³p
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

        // ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â¥ LOG PAYMENT
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
