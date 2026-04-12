package com.tourbooking.booking.model.dto.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourImportDto {
    private String externalId;
    private String tourName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer duration;
    private String startLocation;
    private String endLocation;
    private String transportType;
    private String categoryName;
    private String categoryDescription;
    private String cityName;
    private BigDecimal cityLatitude;
    private BigDecimal cityLongitude;
    private String itinerary;
    private List<String> inclusions;
    private List<String> exclusions;
    private String tips;
    private String paymentPolicy;
    private String cancellationPolicy;
    private String childPolicy;
    private String whyChooseUs;
    private String suitableAges;
    private String bestTime;
    private String weatherInfo;
    private String guideInfo;
    private String metaTitle;
    private String metaDescription;
    private BigDecimal minDepositRate;
    private Integer refundGracePeriod;
    private Double rating;
    private Boolean hasPickup;
    private Boolean hasLunch;
    private Boolean isInstantConfirmation;
    private Boolean isDaily;
    private List<String> highlights;
    private List<String> images;
    private List<TourScheduleImportDto> schedules;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TourScheduleImportDto {
        private String startDate;
        private String endDate;
        private Integer availableSlots;
        private String status;
    }
}
