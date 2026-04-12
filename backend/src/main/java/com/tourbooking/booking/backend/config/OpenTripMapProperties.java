package com.tourbooking.booking.backend.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * [STUB] OpenTripMapProperties - Fields reduced to minimum required by ScheduledTaskService.
 */
@Component
@ConfigurationProperties(prefix = "opentripmap")
@Getter
@Setter
@ToString
public class OpenTripMapProperties {
    private int schedulerMaxCitiesPerRun = 0;
    private List<String> schedulerCities = new ArrayList<>();
}
