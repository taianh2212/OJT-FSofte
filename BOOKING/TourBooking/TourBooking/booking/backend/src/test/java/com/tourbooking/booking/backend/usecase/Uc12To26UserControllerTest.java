package com.tourbooking.booking.backend.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.controller.UserController;
import com.tourbooking.booking.backend.model.dto.request.UserRequest;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC24–UC26 — {@link UserController}.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class Uc12To26UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("UC25 PUT /api/v1/users/{id}")
    void uc25_updateProfile() throws Exception {
        UserResponse res = new UserResponse();
        res.setId(2L);
        res.setFullName("Tran B");
        res.setPhoneNumber("0900000000");
        when(userService.updateUser(eq(2L), any(UserRequest.class))).thenReturn(res);

        UserRequest req = new UserRequest();
        req.setFullName("Tran B");
        req.setPhoneNumber("0900000000");
        req.setAddress("Ha Noi");

        mockMvc.perform(put("/api/v1/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Tran B"));
    }

    @Test
    @DisplayName("UC24 POST /api/v1/users/{id}/documents multipart")
    void uc24_uploadDocument() throws Exception {
        when(userService.uploadDocument(eq(2L), any()))
                .thenReturn("/uploads/doc-1.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "id.pdf",
                "application/pdf",
                "pdf-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/users/2/documents").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("/uploads/doc-1.pdf"));
    }

    @Test
    @DisplayName("UC26 GET /api/v1/users/{id}/loyalty-points")
    void uc26_loyaltyPoints() throws Exception {
        when(userService.getLoyaltyPoints(2L)).thenReturn(1250);

        mockMvc.perform(get("/api/v1/users/2/loyalty-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1250));
    }
}
