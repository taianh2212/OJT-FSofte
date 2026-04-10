package com.tourbooking.booking.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.request.AiChatRequest;
import com.tourbooking.booking.backend.model.dto.response.AiChatResponse;
import com.tourbooking.booking.backend.security.JwtService;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.AiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiChatService aiChatService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.tourbooking.booking.backend.repository.ChatSessionRepository sessionRepository;

    @Test
    void chatReturnsCreatedResponse() throws Exception {
        AiChatResponse aiChatResponse = AiChatResponse.builder().reply("test reply").build();
        when(aiChatService.chat(any(AiChatRequest.class))).thenReturn(aiChatResponse);

        AiChatRequest request = new AiChatRequest();
        request.setUserId(1L);
        request.setMessage("hello");

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.reply").value("test reply"));

        verify(aiChatService).chat(any(AiChatRequest.class));
    }
}
