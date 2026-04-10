package com.tourbooking.booking.controller;

import com.tourbooking.booking.model.dto.request.PaymentRequest;
import com.tourbooking.booking.model.dto.response.ApiResponse;
import com.tourbooking.booking.model.dto.response.PaymentResponse;
import com.tourbooking.booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
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
}
