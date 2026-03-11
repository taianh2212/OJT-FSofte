package com.tourbooking.booking.model.dto.request;

import lombok.Data;

@Data
public class NewsletterRequest {
    private String email;
    private String fullName;
    private String subject;
    private String content;
    private String categoryName;
    private String imageUrls;
    private String highlights;
}
