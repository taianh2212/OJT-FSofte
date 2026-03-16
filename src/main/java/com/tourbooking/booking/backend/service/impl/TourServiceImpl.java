package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.TourMapper;
import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.model.entity.Category;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.repository.CategoryRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepo;
    private final CategoryRepository categoryRepo;

    @Override
    public List<TourResponse> getAllTours() {
        return tourRepo.findAll().stream()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Override
    public TourDetailResponse getTourById(Long id) {
        Tour tour = tourRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return TourMapper.toDetailResponse(tour);
    }

    @Override
    public List<TourResponse> searchTours(String keyword) {
        return tourRepo.findByTourNameContainingIgnoreCase(keyword).stream()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Override
    public List<TourResponse> getToursByCategory(Long categoryId) {
        return tourRepo.findByCategoryId(categoryId).stream()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TourResponse createTour(TourRequest request) {
        Tour tour = TourMapper.toEntity(request);
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            tour.setCategory(category);
        }

        Tour savedTour = tourRepo.save(tour);
        return TourMapper.toResponse(savedTour);
    }

    @Override
    @Transactional
    public TourResponse updateTour(Long id, TourRequest request) {
        Tour existingTour = tourRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourMapper.updateEntityFromRequest(existingTour, request);

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            existingTour.setCategory(category);
        }

        Tour updatedTour = tourRepo.save(existingTour);
        return TourMapper.toResponse(updatedTour);
    }

    @Override
    @Transactional
    public void deleteTour(Long id) {
        if (!tourRepo.existsById(id)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }
        tourRepo.deleteById(id);
    }
}
