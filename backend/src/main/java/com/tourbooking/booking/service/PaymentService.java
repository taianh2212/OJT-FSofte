package com.tourbooking.booking.service;

import com.tourbooking.booking.model.dto.request.PaymentRequest;
import com.tourbooking.booking.model.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse makePayment(PaymentRequest request);
}
