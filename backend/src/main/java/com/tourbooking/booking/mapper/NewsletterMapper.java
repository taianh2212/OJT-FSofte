package com.tourbooking.booking.mapper;

import com.tourbooking.booking.model.dto.request.NewsletterRequest;
import com.tourbooking.booking.model.dto.response.NewsletterResponse;
import com.tourbooking.booking.model.entity.Newsletter;

public class NewsletterMapper {

    public static NewsletterResponse toResponse(Newsletter newsletter) {
        if (newsletter == null)
            return null;
        NewsletterResponse response = new NewsletterResponse();
        response.setId(newsletter.getId());
        response.setEmail(newsletter.getEmail());
        response.setSubscribed(true); // Default to true if entity exists
        return response;
    }

    public static Newsletter toEntity(NewsletterRequest request) {
        if (request == null)
            return null;
        Newsletter newsletter = new Newsletter();
        newsletter.setEmail(request.getEmail());
        // ignore other fields for now as they are not in our Entity
        return newsletter;
    }
}
