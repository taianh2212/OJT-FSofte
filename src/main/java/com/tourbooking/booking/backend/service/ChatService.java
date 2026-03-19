package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.request.ChatMessageRequest;
import com.tourbooking.booking.backend.model.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(ChatMessageRequest request);

    /**
     * If userId is null: return guest (anonymous) messages (UserID = NULL).
     */
    List<ChatMessageResponse> getConversation(Long userId);
}

