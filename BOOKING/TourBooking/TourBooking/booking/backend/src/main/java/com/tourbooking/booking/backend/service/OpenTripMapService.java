package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.response.OpenTripMapFetchSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * [STUB] OpenTripMapService - API integration has been disabled.
 * This class is kept as a shell to avoid breaking ScheduledTaskService in a
 * team project.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenTripMapService {

    private static final String STATUS_DISABLED = "DISABLED_BY_USER";

    /**
     * Stubbed implementation of fetchAndSaveAll.
     * Original logic removed to prioritize manual data entry and AI enrichment.
     */
    public OpenTripMapFetchSummaryResponse fetchAndSaveAll(String cityName) {
        log.info("[OpenTripMap] API Fetch is disabled. Skipping enrichment for: {}", cityName);
        return OpenTripMapFetchSummaryResponse.builder()
                .insertedCount(0)
                .skippedCount(0)
                .status(STATUS_DISABLED)
                .message("OpenTripMap API integration has been disabled.")
                .build();
    }
}
