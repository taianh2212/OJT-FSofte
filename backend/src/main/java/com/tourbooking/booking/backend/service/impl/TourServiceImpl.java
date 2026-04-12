package com.tourbooking.booking.backend.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.TourMapper;
import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.PagedResponse;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.model.entity.City;
import com.tourbooking.booking.backend.model.entity.Category;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.repository.CategoryRepository;
import com.tourbooking.booking.backend.repository.CityRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.service.TourService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepo;
    private final CategoryRepository categoryRepo;
    private final CityRepository cityRepository;

    @Override
    public List<TourResponse> getAllTours() {
        return tourRepo.findAllWithBasicDetails().stream()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Override
    public TourDetailResponse getTourById(Long id) {
        Tour tour = tourRepo.findByIdWithDetails(id)
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
        
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            Category category = categoryRepo.findById(categoryId)
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

        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            Category category = categoryRepo.findById(categoryId)
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

    @Override
    public List<TourResponse> searchToursWithFilters(String keyword, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Double minRating, java.time.LocalDate startDate) {
        return tourRepo.searchToursWithFilters(keyword, minPrice, maxPrice, minRating, startDate).stream()
                .distinct()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Override
    public PagedResponse<TourResponse> browseTours(String keyword,
                                                  java.math.BigDecimal minPrice,
                                                  java.math.BigDecimal maxPrice,
                                                  Double minRating,
                                                  java.time.LocalDate startDate,
                                                  Long categoryId,
                                                  String transportType,
                                                  Long cityId,
                                                  Double lat,
                                                  Double lng,
                                                  Boolean hasPickup,
                                                  Boolean hasLunch,
                                                  Boolean isDaily,
                                                  Boolean isInstantConfirmation,
                                                  String sortBy,
                                                  String sortDir,
                                                  Pageable pageable) {
        String normalizedSortBy = sortBy == null ? "" : sortBy.trim().toLowerCase();

        Page<Tour> page;
        if ("popularity".equals(normalizedSortBy)) {
            page = tourRepo.browseToursByPopularity(keyword, minPrice, maxPrice, minRating, startDate, categoryId, transportType, hasPickup, hasLunch, isDaily, isInstantConfirmation, pageable);
        } else if ("distance".equals(normalizedSortBy)) {
            double[] coords = resolveCoords(cityId, lat, lng);
            page = tourRepo.browseToursByDistance(keyword, minPrice, maxPrice, minRating, startDate, categoryId, transportType, hasPickup, hasLunch, isDaily, isInstantConfirmation, coords[0], coords[1], pageable);
        } else {
            page = tourRepo.browseTours(keyword, minPrice, maxPrice, minRating, startDate, categoryId, transportType, hasPickup, hasLunch, isDaily, isInstantConfirmation, pageable);
        }
        return PagedResponse.<TourResponse>builder()
                .content(page.getContent().stream().map(TourMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private double[] resolveCoords(Long cityId, Double lat, Double lng) {
        if (lat != null && lng != null) {
            return new double[]{lat, lng};
        }
        if (cityId != null) {
            City city = cityRepository.findById(cityId)
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
            return new double[]{city.getCenterLatitude().doubleValue(), city.getCenterLongitude().doubleValue()};
        }
        return new double[]{0.0, 0.0};
    }

    @Override
    public List<TourDetailResponse> compareTours(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tourRepo.findAllById(ids).stream()
                .map(TourMapper::toDetailResponse)
                .toList();
    }
}
