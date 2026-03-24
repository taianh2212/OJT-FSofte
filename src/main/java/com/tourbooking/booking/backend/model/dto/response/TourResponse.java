package com.tourbooking.booking.backend.model.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TourResponse {
    private Long id;
    private String tourName;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String startLocation;
    private String endLocation;
    private Double rating;
    private String transportType;

}
