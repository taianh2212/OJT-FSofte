package com.tourbooking.booking.backend.mapper;

import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.model.entity.TourImage;
import com.tourbooking.booking.backend.model.entity.TourHighlight;
import java.util.stream.Collectors;

public class TourMapper {

    public static TourResponse toResponse(Tour tour) {
        if (tour == null)
            return null;
        TourResponse response = new TourResponse();
        response.setId(tour.getId());
        response.setTourName(tour.getTourName());
        response.setDescription(tour.getDescription());
        response.setPrice(tour.getPrice());
        response.setDuration(tour.getDuration());
        response.setStartLocation(tour.getStartLocation());
        response.setEndLocation(tour.getEndLocation());
        response.setRating(tour.getRating());
        return response;
    }

    public static TourDetailResponse toDetailResponse(Tour tour) {
        if (tour == null)
            return null;
        TourDetailResponse response = new TourDetailResponse();
        response.setId(tour.getId());
        response.setTourName(tour.getTourName());
        response.setDescription(tour.getDescription());
        response.setPrice(tour.getPrice());
        response.setDuration(tour.getDuration());
        response.setStartLocation(tour.getStartLocation());
        response.setEndLocation(tour.getEndLocation());
        response.setRating(tour.getRating());

        if (tour.getCategory() != null) {
            response.setCategoryName(tour.getCategory().getCategoryName());
        }

        if (tour.getImages() != null) {
            response.setImageUrls(tour.getImages().stream().map(TourImage::getImageUrl).collect(Collectors.toList()));
        }

        if (tour.getHighlights() != null) {
            response.setHighlights(
                    tour.getHighlights().stream().map(TourHighlight::getHighlight).collect(Collectors.toList()));
        }

        return response;
    }

    public static Tour toEntity(TourRequest request) {
        if (request == null)
            return null;
        Tour tour = new Tour();
        updateEntityFromRequest(tour, request);
        return tour;
    }

    public static void updateEntityFromRequest(Tour tour, TourRequest request) {
        if (request == null || tour == null)
            return;
        if (request.getTourName() != null) tour.setTourName(request.getTourName());
        if (request.getDescription() != null) tour.setDescription(request.getDescription());
        if (request.getPrice() != null) tour.setPrice(request.getPrice());
        if (request.getDuration() != null) tour.setDuration(request.getDuration());
        if (request.getStartLocation() != null) tour.setStartLocation(request.getStartLocation());
        if (request.getEndLocation() != null) tour.setEndLocation(request.getEndLocation());
        // Category should be handled in the Service layer
    }
}
