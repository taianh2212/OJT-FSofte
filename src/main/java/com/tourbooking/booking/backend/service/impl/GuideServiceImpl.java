package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.model.dto.response.TourScheduleResponse;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import com.tourbooking.booking.backend.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

    private final TourScheduleRepository tourScheduleRepository;

    @Override
    public List<TourScheduleResponse> getAssignedTours(Long guideId) {
        List<TourSchedule> schedules = tourScheduleRepository.findByGuideId(guideId);
        return schedules.stream().map(schedule -> TourScheduleResponse.builder()
                .id(schedule.getId())
                .tourId(schedule.getTour() != null ? schedule.getTour().getId() : null)
                .tourName(schedule.getTour() != null ? schedule.getTour().getTourName() : null)
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .availableSlots(schedule.getAvailableSlots())
                .status(schedule.getStatus() != null ? schedule.getStatus().name() : null)
                .build()
        ).collect(Collectors.toList());
    }
}
