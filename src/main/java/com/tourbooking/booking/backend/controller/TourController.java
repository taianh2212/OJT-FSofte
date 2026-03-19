package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public ApiResponse<List<TourResponse>> getAllTours() {
        return ApiResponse.<List<TourResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved all tours")
                .data(tourService.getAllTours())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TourDetailResponse> getTourById(@PathVariable Long id) {
        return ApiResponse.<TourDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved tour details")
                .data(tourService.getTourById(id))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<TourResponse>> searchTours(@RequestParam String keyword) {
        return ApiResponse.<List<TourResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Search results for: " + keyword)
                .data(tourService.searchTours(keyword))
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<List<TourResponse>> filterTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) java.time.LocalDate startDate) {
        return ApiResponse.<List<TourResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Filtered tours")
                .data(tourService.searchToursWithFilters(keyword, minPrice, maxPrice, minRating, startDate))
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<TourResponse>> getToursByCategory(@PathVariable Long categoryId) {
        return ApiResponse.<List<TourResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Tours in category: " + categoryId)
                .data(tourService.getToursByCategory(categoryId))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TourResponse> createTour(@RequestBody TourRequest request) {
        return ApiResponse.<TourResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tour created successfully")
                .data(tourService.createTour(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TourResponse> updateTour(@PathVariable Long id, @RequestBody TourRequest request) {
        return ApiResponse.<TourResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tour updated successfully")
                .data(tourService.updateTour(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Tour deleted successfully")
                .build();
    }
}
