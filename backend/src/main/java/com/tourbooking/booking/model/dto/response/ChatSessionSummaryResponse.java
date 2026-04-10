package com.tourbooking.booking.model.dto.response;

import java.time.LocalDateTime;

import com.tourbooking.booking.model.entity.enums.ChatSessionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSessionSummaryResponse {
    private Long id;
    private Long userId;
    private String guestId;
    private String customerLabel;
    private ChatSessionStatus status;
    private LocalDateTime lastMessageAt;
}
