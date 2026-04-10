package com.tourbooking.booking.service;

import com.tourbooking.booking.model.dto.request.ChatMessageRequest;
import com.tourbooking.booking.model.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(ChatMessageRequest request);

    /**
     * If userId is null: check guestId.
     */
    List<ChatMessageResponse> getConversation(Long userId, String guestId);

    void escalateToHuman(Long userId, String guestId);

    List<com.tourbooking.booking.model.entity.ChatSession> getActiveSessionsForStaff();

    void closeSession(Long userId, String guestId);
}
