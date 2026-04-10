
package com.tourbooking.booking.service;

import java.util.List;

import com.tourbooking.booking.model.dto.request.NewsletterRequest;
import com.tourbooking.booking.model.dto.response.NewsletterResponse;

public interface NewsletterService {
    List<NewsletterResponse> getAllNewsletters();

    NewsletterResponse getNewsletterById(Long id);

    NewsletterResponse createNewsletter(NewsletterRequest request);

    NewsletterResponse updateNewsletter(Long id, NewsletterRequest request);

    void deleteNewsletter(Long id);
}
