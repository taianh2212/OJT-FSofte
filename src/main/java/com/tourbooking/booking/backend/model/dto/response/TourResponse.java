package com.tourbooking.booking.backend.model.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TourResponse {
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
    private Boolean hasPickup;
    private Boolean hasLunch;
    private Boolean isDaily;
    private Boolean isInstantConfirmation;
    private Long cityId;
    private String cityName;
    private List<String> imageUrls;
}
