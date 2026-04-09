<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/service/NewsletterService.java
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
========
package com.tourbooking.booking.backend.service;

import java.util.List;

import com.tourbooking.booking.backend.model.dto.request.NewsletterRequest;
import com.tourbooking.booking.backend.model.dto.response.NewsletterResponse;

public interface NewsletterService {
    List<NewsletterResponse> getAllNewsletters();

    NewsletterResponse getNewsletterById(Long id);

    NewsletterResponse createNewsletter(NewsletterRequest request);

    NewsletterResponse updateNewsletter(Long id, NewsletterRequest request);

    void deleteNewsletter(Long id);
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/service/NewsletterService.java
