package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.response.CityResponse;
import com.tourbooking.booking.backend.model.dto.response.OpenTripMapFetchSummaryResponse;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.model.entity.City;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.repository.CityRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.service.OpenTripMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminOpenTripMapController {

    private final OpenTripMapService openTripMapService;
    private final CityRepository cityRepository;
    private final TourRepository tourRepository;

    @GetMapping("/fetch-opentripmap")
    public ApiResponse<OpenTripMapFetchSummaryResponse> fetch(@RequestParam("city") String city) {
        OpenTripMapFetchSummaryResponse summary = openTripMapService.fetchAndSaveAll(city);
        return ApiResponse.success(summary);
    }

    @GetMapping("/opentripmap/cities")
    public ApiResponse<List<CityResponse>> cities() {
        List<City> cities = cityRepository.findAllByOrderByCityName();
        List<CityResponse> response = cities.stream()
                .map(this::toCityResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(response);
    }

    @GetMapping("/opentripmap/tours")
    public ApiResponse<List<TourResponse>> tours() {
        List<Tour> tours = tourRepository.findBySourceOrderByTourName("OPENTRIPMAP");
        List<TourResponse> response = tours.stream()
                .map(this::toTourResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(response);
    }

    private CityResponse toCityResponse(City city) {
        CityResponse response = new CityResponse();
        response.setId(city.getId());
        response.setCityName(city.getCityName());
        response.setCenterLatitude(city.getCenterLatitude());
        response.setCenterLongitude(city.getCenterLongitude());
        return response;
    }

    private TourResponse toTourResponse(Tour tour) {
        TourResponse response = new TourResponse();
        response.setId(tour.getId());
        response.setTourName(tour.getTourName());
        response.setDescription(tour.getDescription());
        response.setPrice(tour.getPrice());
        response.setDuration(tour.getDuration());
        response.setStartLocation(tour.getStartLocation());
        response.setEndLocation(tour.getEndLocation());
        response.setRating(tour.getRating());
        response.setTransportType(tour.getTransportType());
        return response;
    }
}
