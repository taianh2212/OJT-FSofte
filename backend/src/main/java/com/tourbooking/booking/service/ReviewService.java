package com.tourbooking.booking.service;

import java.util.List;

import com.tourbooking.booking.model.dto.request.ReviewRequest;
import com.tourbooking.booking.model.dto.response.ReviewResponse;

public interface ReviewService {

    List<ReviewResponse> getReviewsByTour(
            Long tourId,
            Integer minRating,
            Integer maxRating,
            String sortBy,
            String direction);

    List<ReviewResponse> getAllReviews();

    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse updateReview(Long id, ReviewRequest request);

    void deleteReview(Long id);

}
