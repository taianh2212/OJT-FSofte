package com.tourbooking.booking.backend.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.controller.PaymentController;
import com.tourbooking.booking.backend.model.dto.request.PaymentRequest;
import com.tourbooking.booking.backend.model.dto.response.PaymentResponse;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC16–UC18, UC17 PayOS — {@link PaymentController}.
 */
@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class Uc12To26PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("UC16+UC17 POST /api/v1/payments")
    void uc16_uc17_makePayment() throws Exception {
        when(paymentService.makePayment(any(PaymentRequest.class)))
                .thenReturn(PaymentResponse.builder()
                        .paymentId(1L)
                        .bookingId(10L)
                        .amount(new BigDecimal("3500000"))
                        .paymentMethod("VNPAY")
                        .status("SUCCESS")
                        .build());

        PaymentRequest req = new PaymentRequest();
        req.setBookingId(10L);
        req.setPaymentMethod("VNPAY");
        req.setTransactionCode("MOCK-1");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.paymentMethod").value("VNPAY"));
    }

    @Test
    @DisplayName("UC18 POST /api/v1/payments with partial amount")
    void uc18_installment() throws Exception {
        when(paymentService.makePayment(any(PaymentRequest.class)))
                .thenReturn(PaymentResponse.builder()
                        .paymentId(2L)
                        .bookingId(10L)
                        .amount(new BigDecimal("1750000"))
                        .paymentMethod("INSTALLMENT")
                        .status("SUCCESS")
                        .build());

        PaymentRequest req = new PaymentRequest();
        req.setBookingId(10L);
        req.setPaymentMethod("INSTALLMENT");
        req.setAmount(new BigDecimal("1750000"));
        req.setTransactionCode("MOCK-2");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(1750000));
    }

    @Test
    @DisplayName("UC17 POST /api/v1/payments/payos/create")
    void uc17_payosCreate() throws Exception {
        when(paymentService.createPayOSPayment(any(PaymentRequest.class)))
                .thenReturn(PaymentResponse.builder()
                        .paymentId(5L)
                        .bookingId(10L)
                        .checkoutUrl("https://pay.os/checkout")
                        .orderCode(123456L)
                        .amount(new BigDecimal("3500000"))
                        .build());

        PaymentRequest req = new PaymentRequest();
        req.setBookingId(10L);
        req.setPaymentMethod("PAYOS");

        mockMvc.perform(post("/api/v1/payments/payos/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkoutUrl").value("https://pay.os/checkout"))
                .andExpect(jsonPath("$.data.orderCode").value(123456));
    }

    @Test
    @DisplayName("UC17 POST /api/v1/payments/payos/confirm-return")
    void uc17_payosConfirmReturn() throws Exception {
        when(paymentService.confirmPayOsAfterReturn(999L))
                .thenReturn(PaymentResponse.builder()
                        .paymentId(5L)
                        .bookingId(10L)
                        .status("SUCCESS")
                        .build());

        PaymentRequest req = new PaymentRequest();
        req.setOrderCode(999L);

        mockMvc.perform(post("/api/v1/payments/payos/confirm-return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PayOS webhook POST /api/v1/payments/payos/webhook")
    void payosWebhook() throws Exception {
        doNothing().when(paymentService).handlePayOSWebhook(anyString(), any());

        mockMvc.perform(post("/api/v1/payments/payos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OK"));
    }
}
