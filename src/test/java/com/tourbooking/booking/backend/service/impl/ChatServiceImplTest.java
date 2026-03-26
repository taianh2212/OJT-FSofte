package com.tourbooking.booking.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.tourbooking.booking.backend.model.dto.request.ChatMessageRequest;
import com.tourbooking.booking.backend.model.entity.ChatSession;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.ChatSessionStatus;
import com.tourbooking.booking.backend.repository.ChatMessagesRepository;
import com.tourbooking.booking.backend.repository.ChatSessionRepository;
import com.tourbooking.booking.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessagesRepository chatRepo;
    @Mock
    private ChatSessionRepository sessionRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        when(chatRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendMessage_setsSessionToAiWhenFresh() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setUserId(1L);
        request.setMessage("Hello");

        when(sessionRepo.findTopByUser_IdOrderByLastMessageAtDesc(eq(1L))).thenReturn(Optional.empty());
        User user = new User();
        user.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        service.sendMessage(request);

        ArgumentCaptor<ChatSession> sessionCaptor = ArgumentCaptor.forClass(ChatSession.class);
        verify(sessionRepo).save(sessionCaptor.capture());
        assertEquals(ChatSessionStatus.AI, sessionCaptor.getValue().getStatus());
    }

    @Test
    void sendMessage_withKeyword_setsWaitingStaff() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setUserId(2L);
        request.setMessage("Tôi muốn gặp nhân viên");

        when(sessionRepo.findTopByUser_IdOrderByLastMessageAtDesc(eq(2L))).thenReturn(Optional.empty());
        User user = new User();
        user.setId(2L);
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));

        service.sendMessage(request);

        ArgumentCaptor<ChatSession> sessionCaptor = ArgumentCaptor.forClass(ChatSession.class);
        verify(sessionRepo).save(sessionCaptor.capture());
        assertEquals(ChatSessionStatus.WAITING_STAFF, sessionCaptor.getValue().getStatus());
    }

    @Test
    void escalateToHuman_updatesStatus() {
        ChatSession existing = new ChatSession();
        existing.setId(5L);

        when(sessionRepo.findTopByGuestIdOrderByLastMessageAtDesc(eq("g1"))).thenReturn(Optional.of(existing));

        service.escalateToHuman(null, "g1");

        assertEquals(ChatSessionStatus.WAITING_STAFF, existing.getStatus());
    }

    @Test
    void getActiveSessionsForStaff_returnsWaiting() {
        ChatSession session = new ChatSession();
        session.setStatus(ChatSessionStatus.WAITING_STAFF);
        when(sessionRepo.findByStatusOrderByLastMessageAtDesc(ChatSessionStatus.WAITING_STAFF)).thenReturn(List.of(session));

        List<ChatSession> list = service.getActiveSessionsForStaff();
        assertEquals(1, list.size());
    }
}
