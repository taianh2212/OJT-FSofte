package com.tourbooking.booking.backend.model.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Data
public class TourDetailResponse {
    private Long id;
    private String tourName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer duration;
    private String startLocation;
    private String endLocation;
    private Double rating;
    private String transportType;
    private String categoryName;
    private Long cityId;
    private String cityName;
    
    // Premium fields
    private String inclusions;
    private String exclusions;
    private String tips;
    private String itinerary;
    private String paymentPolicy;
    private String cancellationPolicy;
    private String childPolicy;
    
    private Boolean hasPickup;
    private Boolean hasLunch;
    private Boolean isInstantConfirmation;
    private Boolean isDaily;
    
    private BigDecimal minDepositRate;
    private Integer refundGracePeriod;
    private String metaTitle;
    private String metaDescription;
    
    private String whyChooseUs;
    private String suitableAges;
    private String bestTime;
    private String weatherInfo;
    private String guideInfo;

    private List<String> imageUrls;
    private List<String> highlights;
    private List<TourScheduleSummary> schedules;
    private List<TourFaqSummary> faqs;

    @Data
    public static class TourFaqSummary {
        private String question;
        private String answer;
    }

    @Data
    public static class TourScheduleSummary {
        private Long scheduleId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer availableSlots;
        private String status;
    }
}
