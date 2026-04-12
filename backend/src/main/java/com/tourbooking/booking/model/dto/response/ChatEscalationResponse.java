package com.tourbooking.booking.model.dto.response;

import java.time.LocalDateTime;

import com.tourbooking.booking.model.entity.enums.ChatEscalationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatEscalationResponse {
    private Long id;
    private Long userId;
    private String guestId;
    private ChatEscalationStatus status;
    private String requestNote;
    private String meetingPreference;
    private Long assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
