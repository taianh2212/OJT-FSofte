package com.tourbooking.booking.backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleResponse {
    private Long id;
    private Long tourId;
    private String tourName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer availableSlots;
    private String status;
    private String currentProgress;
    private String reportContent;
    private java.time.LocalDateTime reportSubmittedAt;
}
