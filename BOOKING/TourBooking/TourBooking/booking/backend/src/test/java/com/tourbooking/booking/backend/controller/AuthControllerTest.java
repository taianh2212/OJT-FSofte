package com.tourbooking.booking.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.request.AuthRequest;
import com.tourbooking.booking.backend.model.dto.response.AuthResponse;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.security.JwtService;
import com.tourbooking.booking.backend.service.AuthSessionNotificationService;
import com.tourbooking.booking.backend.service.MailService;
import com.tourbooking.booking.backend.service.RateLimiterService;
import com.tourbooking.booking.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private AuthSessionNotificationService authSessionNotificationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginRotatesSessionAndReturnsToken() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);
        user.setIsActive(true);
        user.setEnabled(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(userService.rotateSession("test@example.com")).thenReturn("session-abc");
        when(jwtService.generateToken(user, "session-abc")).thenReturn("jwt-token");

        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("secret");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

        verify(userService).rotateSession("test@example.com");
        verify(jwtService).generateToken(user, "session-abc");
        verify(authSessionNotificationService).notifySessionInvalidated(
                "test@example.com",
                "Tài khoản đã được đăng nhập ở nơi khác. Vui lòng đăng nhập lại.");
    }

    @Test
    void logoutClearsSession() throws Exception {
        doNothing().when(userService).clearSession("test@example.com");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(userService).clearSession("test@example.com");
        verify(authSessionNotificationService).notifySessionInvalidated(
                "test@example.com",
                "Phiên đăng nhập đã bị đăng xuất. Vui lòng đăng nhập lại.");
    }
}
