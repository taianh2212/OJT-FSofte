package com.tourbooking.booking.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.Payment;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import com.tourbooking.booking.backend.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.PaymentLogRepository;
import com.tourbooking.booking.backend.repository.PaymentRepository;
import com.tourbooking.booking.backend.service.LoyaltyService;
import com.tourbooking.booking.backend.service.MailService;
import com.tourbooking.booking.backend.service.PayOSService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm tra luồng: webhook PayOS hợp lệ → payment SUCCESS, booking CONFIRMED, gửi mail.
 * Không gọi PayOS thật; chữ ký được giả lập bằng {@code verifyPayOsDataSignature == true}.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplPayOsFlowTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentLogRepository paymentLogRepository;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private PayOSService payOSService;
    @Mock
    private MailService mailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                bookingRepository,
                paymentRepository,
                paymentLogRepository,
                loyaltyService,
                payOSService,
                mailService,
                objectMapper
        );
    }

    @Test
    void handlePayOSWebhook_whenPaid_updatesBookingAndSendsEmail() {
        User user = new User();
        user.setId(5L);
        user.setEmail("khach@test.com");
        user.setFullName("Nguyen Van A");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(BigDecimal.valueOf(3000));

        Payment payment = new Payment();
        payment.setId(99L);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(3000));
        payment.setPaymentMethod("PAYOS");
        payment.setTransactionCode("PAYOS-123");
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByTransactionCode("PAYOS-123")).thenReturn(Optional.of(payment));
        when(payOSService.verifyPayOsDataSignature(any(), anyString())).thenReturn(true);

        String payload = """
                {
                  "code": "00",
                  "desc": "success",
                  "success": true,
                  "data": {
                    "orderCode": 123,
                    "amount": 3000,
                    "description": "VQRIO123",
                    "accountNumber": "12345678",
                    "reference": "TF230204212323",
                    "transactionDateTime": "2023-02-04 18:25:00",
                    "currency": "VND",
                    "paymentLinkId": "124c33293c43417ab7879e14c8d9eb18",
                    "code": "00",
                    "desc": "Thành công",
                    "counterAccountBankId": "",
                    "counterAccountBankName": "",
                    "counterAccountName": "",
                    "counterAccountNumber": "",
                    "virtualAccountName": "",
                    "virtualAccountNumber": ""
                  },
                  "signature": "412e915d2871504ed31be63c8f62a149a4410d34c4c42affc9006ef9917eaa03"
                }
                """;

        paymentService.handlePayOSWebhook(payload, "");

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).save(booking);
        verify(paymentRepository).save(payment);
        verify(mailService).sendPaymentSuccessEmail(
                eq("khach@test.com"),
                eq("Nguyen Van A"),
                eq(10L),
                eq(BigDecimal.valueOf(3000))
        );
        verify(loyaltyService).addPoint(eq(5L), eq(0));
        verify(paymentLogRepository).save(any());
    }

    @Test
    void confirmPayOsAfterReturn_whenApiReturnsPaid_confirmsAndSendsEmail() {
        User user = new User();
        user.setId(7L);
        user.setEmail("user2@test.com");
        user.setFullName("Tran B");

        Booking booking = new Booking();
        booking.setId(20L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(100L);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(50000));
        payment.setTransactionCode("PAYOS-999888");
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByTransactionCode("PAYOS-999888")).thenReturn(Optional.of(payment));
        when(payOSService.fetchPaymentRequestByOrderCode(999888L)).thenReturn(
                Optional.of(objectMapper.createObjectNode()
                        .put("status", "PAID")
                        .put("orderCode", 999888)
                        .put("amount", 50000))
        );

        paymentService.confirmPayOsAfterReturn(999888L);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(mailService).sendPaymentSuccessEmail(
                eq("user2@test.com"),
                eq("Tran B"),
                eq(20L),
                eq(BigDecimal.valueOf(50000))
        );
    }
}
