package com.tourbooking.booking.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.request.ChatEscalationReplyRequest;
import com.tourbooking.booking.backend.model.dto.response.ChatSessionSummaryResponse;
import com.tourbooking.booking.backend.model.entity.ChatSession;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.ChatSessionStatus;
import com.tourbooking.booking.backend.repository.ChatSessionRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.ChatNotificationService;
import com.tourbooking.booking.backend.service.ChatService;
import com.tourbooking.booking.backend.security.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(controllers = AdminChatController.class)
@Import({
        com.tourbooking.booking.backend.config.SecurityConfig.class,
        com.tourbooking.booking.backend.security.JwtAuthenticationFilter.class
})
class AdminChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private ChatSessionRepository sessionRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ChatNotificationService notificationService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEscalations_returnsWaitingSessions() throws Exception {
        ChatSession session = new ChatSession();
        session.setId(10L);
        session.setStatus(ChatSessionStatus.WAITING_STAFF);
        session.setLastMessageAt(LocalDateTime.now());
        when(chatService.getActiveSessionsForStaff()).thenReturn(List.of(session));

        mockMvc.perform(get("/api/v1/admin/chat/escalations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void replyEscalation_postsMessage() throws Exception {
        ChatEscalationReplyRequest request = new ChatEscalationReplyRequest();
        request.setMessage("Reply");
        ChatSession session = new ChatSession();
        session.setId(5L);
        session.setStatus(ChatSessionStatus.WAITING_STAFF);
        session.setLastMessageAt(LocalDateTime.now());
        when(sessionRepository.findById(5L)).thenReturn(java.util.Optional.of(session));
        User staff = new User();
        staff.setId(2L);
        staff.setEmail("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(java.util.Optional.of(staff));

        mockMvc.perform(post("/api/v1/admin/chat/escalations/5/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("STAFF_CHATTING"));

        org.mockito.ArgumentCaptor<com.tourbooking.booking.backend.model.dto.request.ChatMessageRequest> captor =
                org.mockito.ArgumentCaptor.forClass(com.tourbooking.booking.backend.model.dto.request.ChatMessageRequest.class);
        verify(chatService).sendMessage(captor.capture());
        assertEquals("STAFF", captor.getValue().getSenderType());
        assertEquals("Reply", captor.getValue().getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void notifications_stream_returnsEmitter() throws Exception {
        when(notificationService.subscribe()).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/admin/chat/escalations/notifications/stream"))
                .andExpect(status().isOk());
    }
}
