package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.mapper.TourMapper;
import com.tourbooking.booking.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.model.dto.response.TourResponse;
import com.tourbooking.booking.model.entity.Tour;
import com.tourbooking.booking.repository.TourRepo;
import com.tourbooking.booking.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepo tourRepo;

    @Override
    public List<TourResponse> getAllTours() {
        return tourRepo.findAll().stream()
                .map(TourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TourDetailResponse getTourById(Long id) {
        Tour tour = tourRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));
        return TourMapper.toDetailResponse(tour);
    }

    @Override
    public List<TourResponse> searchTours(String keyword) {
        return tourRepo.findByTourNameContainingIgnoreCase(keyword).stream()
                .map(TourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TourResponse> getToursByCategory(Long categoryId) {
        return tourRepo.findByCategoryId(categoryId).stream()
                .map(TourMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TourResponse createTour(TourResponse request) {
        Tour tour = TourMapper.toEntity(request);
        Tour savedTour = tourRepo.save(tour);
        return TourMapper.toResponse(savedTour);
    }

    @Override
    public TourResponse updateTour(Long id, TourResponse request) {
        Tour existingTour = tourRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));

        existingTour.setTourName(request.getTourName());
        existingTour.setDescription(request.getDescription());
        existingTour.setPrice(request.getPrice());
        existingTour.setDuration(request.getDuration());
        existingTour.setStartLocation(request.getStartLocation());
        existingTour.setEndLocation(request.getEndLocation());
        existingTour.setRating(request.getRating());

        Tour updatedTour = tourRepo.save(existingTour);
        return TourMapper.toResponse(updatedTour);
    }

    @Override
    public void deleteTour(Long id) {
        if (!tourRepo.existsById(id)) {
            throw new RuntimeException("Tour not found with id: " + id);
        }
        tourRepo.deleteById(id);
    }
}