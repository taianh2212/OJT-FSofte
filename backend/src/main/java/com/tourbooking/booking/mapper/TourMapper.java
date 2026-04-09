package com.tourbooking.booking.mapper;

import com.tourbooking.booking.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.model.dto.response.TourResponse;
import com.tourbooking.booking.model.entity.Tour;
import com.tourbooking.booking.model.entity.TourImage;
import com.tourbooking.booking.model.entity.TourHighlight;
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

    public static Tour toEntity(TourResponse response) {
        if (response == null)
            return null;
        Tour tour = new Tour();
        tour.setId(response.getId());
        tour.setTourName(response.getTourName());
        tour.setDescription(response.getDescription());
        tour.setPrice(response.getPrice());
        tour.setDuration(response.getDuration());
        tour.setStartLocation(response.getStartLocation());
        tour.setEndLocation(response.getEndLocation());
        tour.setRating(response.getRating());
        return tour;
    }
}
