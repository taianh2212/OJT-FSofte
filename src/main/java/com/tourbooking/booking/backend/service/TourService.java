package com.tourbooking.booking.backend.service;

import java.util.List;

import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;

public interface TourService {
    List<TourResponse> getAllTours();

    TourDetailResponse getTourById(Long id);

    List<TourResponse> searchTours(String keyword);

    List<TourResponse> getToursByCategory(Long categoryId);

    TourResponse createTour(TourRequest request);

    TourResponse updateTour(Long id, TourRequest request);

    void deleteTour(Long id);
}
