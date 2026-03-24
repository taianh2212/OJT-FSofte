package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.response.TourScheduleResponse;
import java.util.List;

public interface GuideService {
    List<TourScheduleResponse> getAssignedTours(Long guideId);
}
