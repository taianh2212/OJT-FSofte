package com.tourbooking.booking.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tourbooking.booking.backend.model.dto.request.ChatEscalationReplyRequest;
import com.tourbooking.booking.backend.model.dto.request.ChatEscalationRequest;
import com.tourbooking.booking.backend.model.entity.ChatEscalation;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.ChatEscalationStatus;
import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import com.tourbooking.booking.backend.repository.ChatEscalationRepository;
import com.tourbooking.booking.backend.repository.ChatMessagesRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.ChatService;

@ExtendWith(MockitoExtension.class)
class ChatEscalationServiceImplTest {

    @Mock
    private ChatEscalationRepository escalationRepo;
    @Mock
    private ChatMessagesRepository chatRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatEscalationServiceImpl service;

    @BeforeEach
    void setUp() {
        when(escalationRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void requestEscalation_createsOpenForGuestWhenNoneExists() {
        when(escalationRepo.findTopByGuestIdAndStatusNotInOrderByUpdatedAtDesc(eq("guest-1"), any()))
                .thenReturn(Optional.empty());

        ChatEscalationRequest request = new ChatEscalationRequest();
        request.setGuestId("guest-1");
        request.setMeetingPreference("Tomorrow afternoon");

        var result = service.requestEscalation(request);

        assertEquals(ChatEscalationStatus.OPEN, result.getStatus());
        assertEquals("guest-1", result.getGuestId());
        assertEquals("Tomorrow afternoon", result.getMeetingPreference());
    }

    @Test
    void reply_assignsStaffAndMovesToInReview() {
        ChatEscalation escalation = new ChatEscalation();
        escalation.setId(1L);
        escalation.setStatus(ChatEscalationStatus.OPEN);

        User staff = new User();
        staff.setId(5L);
        staff.setRole(UserRole.ADMIN);

        when(escalationRepo.findById(1L)).thenReturn(Optional.of(escalation));
        when(userRepo.findById(5L)).thenReturn(Optional.of(staff));

        ChatEscalationReplyRequest request = new ChatEscalationReplyRequest();
        request.setMessage("We are on it.");

        var response = service.reply(1L, 5L, request);

        assertEquals(ChatEscalationStatus.IN_REVIEW, response.getStatus());
        assertEquals(5L, response.getAssignedStaffId());
    }

    @Test
    void resolve_marksAsResolved() {
        ChatEscalation escalation = new ChatEscalation();
        escalation.setId(2L);
        escalation.setStatus(ChatEscalationStatus.IN_REVIEW);

        User staff = new User();
        staff.setId(9L);
        staff.setRole(UserRole.ADMIN);

        when(escalationRepo.findById(2L)).thenReturn(Optional.of(escalation));
        when(userRepo.findById(9L)).thenReturn(Optional.of(staff));

        var response = service.resolve(2L, 9L);

        assertEquals(ChatEscalationStatus.RESOLVED, response.getStatus());
        assertEquals(9L, response.getAssignedStaffId());
    }
}
