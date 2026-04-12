package com.tourbooking.booking.model.dto.request;

import lombok.Data;

@Data
public class ChatEscalationCloseRequest {
    private Long userId;
    private String guestId;
}
