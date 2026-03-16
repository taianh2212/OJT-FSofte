package com.tourbooking.booking.service;

import java.util.List;

import com.tourbooking.booking.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.model.dto.response.TourResponse;

public interface TourService {
    List<TourResponse> getAllTours();

    TourDetailResponse getTourById(Long id);

    List<TourResponse> searchTours(String keyword);

    List<TourResponse> getToursByCategory(Long categoryId);

    TourResponse createTour(TourResponse request);

    TourResponse updateTour(Long id, TourResponse request);

    void deleteTour(Long id);

}
