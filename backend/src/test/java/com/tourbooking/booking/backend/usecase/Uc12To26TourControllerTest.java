package com.tourbooking.booking.backend.usecase;

import com.tourbooking.booking.backend.controller.TourController;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.service.TourService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC12–UC13 — tour + schedules for checkout (UC12 contact fields are UI-only).
 */
@WebMvcTest(TourController.class)
@AutoConfigureMockMvc(addFilters = false)
class Uc12To26TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourService tourService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("UC13 GET /api/v1/tours/{id} with schedules")
    void uc13_tourDetailSchedules() throws Exception {
        TourDetailResponse.TourScheduleSummary s = new TourDetailResponse.TourScheduleSummary();
        s.setScheduleId(100L);
        s.setStartDate(LocalDate.parse("2026-08-25"));
        s.setAvailableSlots(10);

        TourDetailResponse detail = new TourDetailResponse();
        detail.setId(1L);
        detail.setTourName("Da Nang 3N2D");
        detail.setPrice(java.math.BigDecimal.valueOf(3_500_000));
        detail.setSchedules(List.of(s));

        when(tourService.getTourById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/tours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tourName").value("Da Nang 3N2D"))
                .andExpect(jsonPath("$.data.schedules[0].scheduleId").value(100))
                .andExpect(jsonPath("$.data.schedules[0].availableSlots").value(10));
    }

    @Test
    @DisplayName("UC13 GET /api/v1/tours")
    void uc13_listTours() throws Exception {
        when(tourService.getAllTours()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
