package com.tourbooking.booking.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tour.import")
public class TourImportProperties {
    private boolean enabled = false;
    private String directory = "data/tours";
}
