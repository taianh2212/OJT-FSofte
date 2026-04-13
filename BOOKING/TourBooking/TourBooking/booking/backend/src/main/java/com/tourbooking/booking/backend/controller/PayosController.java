package com.tourbooking.booking.backend.controller;

import vn.payos.PayOS;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.CheckoutResponseData;
import com.tourbooking.booking.backend.config.PayOSProperties;
import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/payos")
@RequiredArgsConstructor
public class PayosController {

    private final PayOS payOS;
    private final BookingRepository bookingRepository;
    private final PayOSProperties payOSProperties;

    @PostMapping("/create")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> createPaymentLink(@RequestBody Map<String, Object> request) {
        try {
            Long bookingId = Long.valueOf(request.get("bookingId").toString());
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

            int amount = booking.getTotalPrice().intValue();
            if (request.get("amount") != null) {
                amount = Double.valueOf(request.get("amount").toString()).intValue();
            }

            // PayOS 1.x requires long orderCode
            long orderCode = System.currentTimeMillis() / 1000; 
            
            String tourName = booking.getSchedule().getTour().getTourName();
            String description = "Thanh toan " + tourName;
            if (description.length() > 25) description = description.substring(0, 22) + "...";

            ItemData item = ItemData.builder()
                    .name(tourName)
                    .quantity(booking.getNumberOfPeople())
                    .price(amount / booking.getNumberOfPeople())
                    .build();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl(payOSProperties.getReturnUrl())
                    .cancelUrl(payOSProperties.getCancelUrl())
                    .item(item)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);

            return ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("Payment link created")
                    .data(Map.of(
                            "checkoutUrl", data.getCheckoutUrl(),
                            "orderCode", orderCode,
                            "amount", amount
                    ))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create PayOS link", e);
            throw new RuntimeException("PayOS creation failed: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> body) {
        log.info("Received PayOS webhook: {}", body);
        // Implement webhook logic here
    }
}
