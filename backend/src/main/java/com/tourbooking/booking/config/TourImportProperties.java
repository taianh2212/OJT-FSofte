package com.tourbooking.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tour.import")
@Data
public class TourImportProperties {
    private boolean enabled = true;
    private String directory = "data/tours";
}
