package com.tourbooking.booking.backend.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TourRequest {
    private String tourName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer duration;
    private String startLocation;
    private String endLocation;
    private String transportType;
    private Long categoryId;
    
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
}
