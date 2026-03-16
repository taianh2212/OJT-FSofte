package com.tourbooking.booking.backend.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TourRequest {
    private String tourName;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String startLocation;
    private String endLocation;
    private Long categoryId;
}
