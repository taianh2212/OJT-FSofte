package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.request.PaymentRequest;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.PaymentResponse;
import com.tourbooking.booking.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // UC17 + UC18
    @PostMapping
    public ApiResponse<PaymentResponse> makePayment(@RequestBody PaymentRequest request) {

        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Payment processed successfully")
                .data(paymentService.makePayment(request))
                .build();
    }

    @PostMapping("/payos/create")
    public ApiResponse<PaymentResponse> createPayOSPayment(@RequestBody PaymentRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("PayOS checkout created")
                .data(paymentService.createPayOSPayment(request))
                .build();
    }

    @PostMapping(value = "/payos/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<String> payOSWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        paymentService.handlePayOSWebhook(payload, signature);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Webhook processed")
                .data("OK")
                .build();
    }
}