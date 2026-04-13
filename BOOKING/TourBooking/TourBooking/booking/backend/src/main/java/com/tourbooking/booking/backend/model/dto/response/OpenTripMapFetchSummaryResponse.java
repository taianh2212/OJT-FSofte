package com.tourbooking.booking.backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenTripMapFetchSummaryResponse {
    private int insertedCount;
    private int skippedCount;
    private String status;
    private String message;
}
