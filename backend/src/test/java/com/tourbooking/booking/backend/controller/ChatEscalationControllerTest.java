package com.tourbooking.booking.backend.controller;
package com.tourbooking.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.response.ChatEscalationResponse;
import com.tourbooking.booking.backend.model.entity.enums.ChatEscalationStatus;
import com.tourbooking.booking.backend.config.SecurityConfig;
import com.tourbooking.booking.backend.security.JwtAuthenticationFilter;
import com.tourbooking.booking.backend.security.JwtService;
import com.tourbooking.booking.backend.service.ChatEscalationService;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.model.dto.response.ChatEscalationResponse;
import com.tourbooking.booking.model.entity.enums.ChatEscalationStatus;
import com.tourbooking.booking.config.SecurityConfig;
import com.tourbooking.booking.security.JwtAuthenticationFilter;
import com.tourbooking.booking.security.JwtService;
import com.tourbooking.booking.service.ChatEscalationService;
import com.tourbooking.booking.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        ChatEscalationController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class ChatEscalationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatEscalationService chatEscalationService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createEscalation_returnsCreated() throws Exception {
        ChatEscalationResponse response = ChatEscalationResponse.builder()
                .id(1L)
                .status(ChatEscalationStatus.OPEN)
                .build();

        when(chatEscalationService.requestEscalation(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/escalations")
                        .contentType("application/json")
                        .content("{\"guestId\":\"guest-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

}
