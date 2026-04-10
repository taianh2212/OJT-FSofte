package com.tourbooking.booking.service;

import com.tourbooking.booking.model.dto.request.AiChatRequest;
import com.tourbooking.booking.model.dto.response.AiChatResponse;

public interface AiChatService {
    AiChatResponse chat(AiChatRequest request);
}

