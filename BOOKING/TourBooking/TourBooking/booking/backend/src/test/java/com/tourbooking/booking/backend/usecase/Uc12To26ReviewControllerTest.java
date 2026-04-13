package com.tourbooking.booking.backend.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.controller.ReviewController;
import com.tourbooking.booking.backend.model.dto.request.ReviewRequest;
import com.tourbooking.booking.backend.model.dto.response.ReviewResponse;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC23 — {@link ReviewController}.
 */
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class Uc12To26ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("UC23 GET /api/v1/reviews/tour/{tourId}")
    void uc23_listByTour() throws Exception {
        ReviewResponse r = new ReviewResponse();
        r.setReviewId(1L);
        r.setTourId(3L);
        r.setUserName("Nguyen A");
        r.setRating(5);
        r.setComment("Great");
        r.setCreatedAt(LocalDateTime.of(2026, 4, 1, 12, 0));
        when(reviewService.getReviewsByTour(eq(3L), eq(null), eq(null), eq("createdAt"), eq("desc")))
                .thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/reviews/tour/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reviewId").value(1))
                .andExpect(jsonPath("$.data[0].comment").value("Great"));
    }

    @Test
    @DisplayName("UC23 POST /api/v1/reviews")
    void uc23_createReview() throws Exception {
        ReviewResponse saved = new ReviewResponse();
        saved.setReviewId(10L);
        saved.setRating(4);
        saved.setComment("Ok");
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(saved);

        ReviewRequest req = new ReviewRequest();
        req.setTourId(3L);
        req.setUserId(1L);
        req.setRating(4);
        req.setComment("Ok");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.reviewId").value(10));
    }
}
