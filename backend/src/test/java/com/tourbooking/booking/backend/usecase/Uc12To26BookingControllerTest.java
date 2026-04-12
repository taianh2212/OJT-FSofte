package com.tourbooking.booking.backend.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.controller.BookingController;
import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.request.RefundRequest;
import com.tourbooking.booking.backend.model.dto.request.VoucherRequest;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import com.tourbooking.booking.backend.model.dto.response.VoucherResponse;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC13–UC15, UC17, UC19–UC22 — contract {@link BookingController}.
 */
@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class Uc12To26BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("UC17 POST /api/v1/bookings creates booking")
    void uc17_createBooking() throws Exception {
        BookingResponse res = new BookingResponse();
        res.setId(10L);
        res.setUserId(1L);
        res.setScheduleId(2L);
        res.setTourId(3L);
        res.setNumberOfPeople(2);
        res.setStatus(BookingStatus.PENDING);
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(res);

        BookingRequest req = new BookingRequest();
        req.setUserId(1L);
        req.setScheduleId(2L);
        req.setNumberOfPeople(2);
        req.setVoucherCode("SUMMER");

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.tourId").value(3));
    }

    @Test
    @DisplayName("UC15 POST /api/v1/bookings/apply-voucher")
    void uc15_applyVoucher() throws Exception {
        when(bookingService.applyVoucher(any(VoucherRequest.class)))
                .thenReturn(VoucherResponse.builder()
                        .isValid(true)
                        .discountAmount(new BigDecimal("500000"))
                        .finalTotal(new BigDecimal("3000000"))
                        .message("OK")
                        .build());

        VoucherRequest req = new VoucherRequest();
        req.setVoucherCode("SAVE10");
        req.setCurrentTotal(new BigDecimal("3500000"));

        mockMvc.perform(post("/api/v1/bookings/apply-voucher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(500000));
    }

    @Test
    @DisplayName("UC19 GET /api/v1/bookings/user/{userId}")
    void uc19_listByUser() throws Exception {
        BookingResponse b = new BookingResponse();
        b.setId(1L);
        b.setTourName("Test Tour");
        b.setStatus(BookingStatus.COMPLETED);
        when(bookingService.getBookingsByUserId(5L)).thenReturn(List.of(b));

        mockMvc.perform(get("/api/v1/bookings/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].tourName").value("Test Tour"));
    }

    @Test
    @DisplayName("UC20 POST /api/v1/bookings/{id}/cancel")
    void uc20_cancel() throws Exception {
        BookingResponse res = new BookingResponse();
        res.setId(7L);
        res.setStatus(BookingStatus.CANCELLED);
        when(bookingService.cancelBooking(7L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/bookings/7/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("UC21 POST /api/v1/bookings/{id}/refund")
    void uc21_refund() throws Exception {
        BookingResponse res = new BookingResponse();
        res.setId(7L);
        res.setStatus(BookingStatus.REFUND_REQUESTED);
        when(bookingService.requestRefund(eq(7L), any(RefundRequest.class))).thenReturn(res);

        RefundRequest req = new RefundRequest();
        req.setBookingId(7L);
        req.setReason("MB - 0123 - NGUYEN VAN A");

        mockMvc.perform(post("/api/v1/bookings/7/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REFUND_REQUESTED"));
    }

    @Test
    @DisplayName("UC22 GET /api/v1/bookings/{id}/invoice returns PDF")
    void uc22_invoicePdf() throws Exception {
        byte[] pdf = "%PDF-1.4 test".getBytes(StandardCharsets.UTF_8);
        when(bookingService.downloadInvoice(99L)).thenReturn(pdf);

        mockMvc.perform(get("/api/v1/bookings/99/invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("invoice_99.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));
    }
}
