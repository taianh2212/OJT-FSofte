package com.tourbooking.booking.backend.mapper;

import com.tourbooking.booking.backend.model.dto.request.TourRequest;
import com.tourbooking.booking.backend.model.dto.response.TourDetailResponse;
import com.tourbooking.booking.backend.model.dto.response.TourResponse;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.model.entity.TourImage;
import com.tourbooking.booking.backend.model.entity.TourHighlight;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
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
        response.setOriginalPrice(tour.getOriginalPrice());
        response.setDuration(tour.getDuration());
        response.setStartLocation(tour.getStartLocation());
        response.setEndLocation(tour.getEndLocation());
        response.setRating(tour.getRating());
        response.setTransportType(tour.getTransportType());
        response.setHasPickup(tour.getHasPickup());
        response.setHasLunch(tour.getHasLunch());
        response.setIsDaily(tour.getIsDaily());
        response.setIsInstantConfirmation(tour.getIsInstantConfirmation());
        if (tour.getImages() != null) {
            response.setImageUrls(tour.getImages().stream().map(TourImage::getImageUrl).collect(Collectors.toList()));
        }
        if (tour.getCity() != null) {
            response.setCityId(tour.getCity().getId());
            response.setCityName(tour.getCity().getCityName());
        }
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
        response.setOriginalPrice(tour.getOriginalPrice());
        response.setDuration(tour.getDuration());
        response.setStartLocation(tour.getStartLocation());
        response.setEndLocation(tour.getEndLocation());
        response.setRating(tour.getRating());
        response.setTransportType(tour.getTransportType());
        if (tour.getCity() != null) {
            response.setCityId(tour.getCity().getId());
            response.setCityName(tour.getCity().getCityName());
        }

        // New fields
        response.setInclusions(tour.getInclusions());
        response.setExclusions(tour.getExclusions());
        response.setTips(tour.getTips());
        response.setItinerary(tour.getItinerary());
        response.setPaymentPolicy(tour.getPaymentPolicy());
        response.setCancellationPolicy(tour.getCancellationPolicy());
        response.setChildPolicy(tour.getChildPolicy());
        response.setHasPickup(tour.getHasPickup());
        response.setHasLunch(tour.getHasLunch());
        response.setIsInstantConfirmation(tour.getIsInstantConfirmation());
        response.setIsDaily(tour.getIsDaily());
        response.setMinDepositRate(tour.getMinDepositRate());
        response.setRefundGracePeriod(tour.getRefundGracePeriod());
        response.setMetaTitle(tour.getMetaTitle());
        response.setMetaDescription(tour.getMetaDescription());
        response.setWhyChooseUs(tour.getWhyChooseUs());
        response.setSuitableAges(tour.getSuitableAges());
        response.setBestTime(tour.getBestTime());
        response.setWeatherInfo(tour.getWeatherInfo());
        response.setGuideInfo(tour.getGuideInfo());

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

        if (tour.getSchedules() != null) {
            response.setSchedules(tour.getSchedules().stream().map(TourMapper::toScheduleSummary).toList());
        }

        if (tour.getFaqs() != null) {
            response.setFaqs(tour.getFaqs().stream().map(TourMapper::toFaqSummary).collect(Collectors.toList()));
        }

        return response;
    }

    private static TourDetailResponse.TourFaqSummary toFaqSummary(com.tourbooking.booking.backend.model.entity.TourFaq faq) {
        TourDetailResponse.TourFaqSummary s = new TourDetailResponse.TourFaqSummary();
        s.setQuestion(faq.getQuestion());
        s.setAnswer(faq.getAnswer());
        return s;
    }

    private static TourDetailResponse.TourScheduleSummary toScheduleSummary(TourSchedule schedule) {
        TourDetailResponse.TourScheduleSummary s = new TourDetailResponse.TourScheduleSummary();
        s.setScheduleId(schedule.getId());
        s.setStartDate(schedule.getStartDate());
        s.setEndDate(schedule.getEndDate());
        s.setAvailableSlots(schedule.getAvailableSlots());
        s.setStatus(schedule.getStatus() == null ? null : schedule.getStatus().name());
        return s;
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
        if (request.getOriginalPrice() != null) tour.setOriginalPrice(request.getOriginalPrice());
        if (request.getDuration() != null) tour.setDuration(request.getDuration());
        if (request.getStartLocation() != null) tour.setStartLocation(request.getStartLocation());
        if (request.getEndLocation() != null) tour.setEndLocation(request.getEndLocation());
        if (request.getTransportType() != null) tour.setTransportType(request.getTransportType());

        if (request.getInclusions() != null) tour.setInclusions(request.getInclusions());
        if (request.getExclusions() != null) tour.setExclusions(request.getExclusions());
        if (request.getTips() != null) tour.setTips(request.getTips());
        if (request.getItinerary() != null) tour.setItinerary(request.getItinerary());
        if (request.getPaymentPolicy() != null) tour.setPaymentPolicy(request.getPaymentPolicy());
        if (request.getCancellationPolicy() != null) tour.setCancellationPolicy(request.getCancellationPolicy());
        if (request.getChildPolicy() != null) tour.setChildPolicy(request.getChildPolicy());

        if (request.getHasPickup() != null) tour.setHasPickup(request.getHasPickup());
        if (request.getHasLunch() != null) tour.setHasLunch(request.getHasLunch());
        if (request.getIsInstantConfirmation() != null) tour.setIsInstantConfirmation(request.getIsInstantConfirmation());
        if (request.getIsDaily() != null) tour.setIsDaily(request.getIsDaily());

        if (request.getMinDepositRate() != null) tour.setMinDepositRate(request.getMinDepositRate());
        if (request.getRefundGracePeriod() != null) tour.setRefundGracePeriod(request.getRefundGracePeriod());
        if (request.getMetaTitle() != null) tour.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) tour.setMetaDescription(request.getMetaDescription());
        
        // Note: Adding these manually as they might be part of extended request later
        // For now, Seeder uses them directly.
    }
}
