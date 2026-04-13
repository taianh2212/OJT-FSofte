package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.service.AuthSessionNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.security.JwtService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(AuthSessionNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthSessionNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthSessionNotificationService authSessionNotificationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void eventsEndpointDelegatesToService() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(authSessionNotificationService.subscribe("jwt-token")).thenReturn(emitter);

        mockMvc.perform(get("/api/v1/auth/events").param("token", "jwt-token"))
                .andExpect(request().asyncStarted());

        verify(authSessionNotificationService).subscribe("jwt-token");
    }
}
