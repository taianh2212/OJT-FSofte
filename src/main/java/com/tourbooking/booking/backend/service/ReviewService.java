package com.tourbooking.booking.backend.service;

import java.util.List;

import com.tourbooking.booking.backend.model.dto.request.ReviewRequest;
import com.tourbooking.booking.backend.model.dto.response.ReviewResponse;

public interface ReviewService {

    List<ReviewResponse> getReviewsByTour(
            Long tourId,
            Integer minRating,
            Integer maxRating,
            String sortBy,
            String direction);

    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse updateReview(Long id, ReviewRequest request);

    void deleteReview(Long id);

}
