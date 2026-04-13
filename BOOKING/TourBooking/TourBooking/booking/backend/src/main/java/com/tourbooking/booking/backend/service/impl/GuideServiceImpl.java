package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.model.dto.response.TourScheduleResponse;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.TourActivityImage;
import com.tourbooking.booking.backend.model.entity.enums.TourStatus;
import com.tourbooking.booking.backend.repository.TourActivityImageRepository;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import com.tourbooking.booking.backend.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

    private final TourScheduleRepository tourScheduleRepository;
    private final TourActivityImageRepository tourActivityImageRepository;

    @Override
    public List<TourScheduleResponse> getAssignedTours(Long guideId) {
        List<TourSchedule> schedules = tourScheduleRepository.findByGuideId(guideId);
        return schedules.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateTourProgress(Long guideId, Long scheduleId, String progress) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));

        if (!schedule.getGuide().getId().equals(guideId)) {
            throw new RuntimeException("You are not assigned to this tour");
        }

        schedule.setCurrentProgress(progress);
        if (schedule.getStatus() == TourStatus.FULL || schedule.getStatus() == TourStatus.OPEN) {
            schedule.setStatus(TourStatus.IN_PROGRESS);
        }
        tourScheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void uploadTourPhotos(Long guideId, Long scheduleId, List<MultipartFile> photos) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));

        if (!schedule.getGuide().getId().equals(guideId)) {
            throw new RuntimeException("You are not assigned to this tour");
        }

        for (MultipartFile photo : photos) {
            // In a real app, upload to S3/Cloudinary and get URL
            // Here we mock the URL
            String mockUrl = "/uploads/" + photo.getOriginalFilename();
            TourActivityImage image = new TourActivityImage();
            image.setSchedule(schedule);
            image.setImageUrl(mockUrl);
            tourActivityImageRepository.save(image);
        }
    }

    @Override
    @Transactional
    public void submitTourReport(Long guideId, Long scheduleId, String reportContent) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));

        if (!schedule.getGuide().getId().equals(guideId)) {
            throw new RuntimeException("You are not assigned to this tour");
        }

        schedule.setReportContent(reportContent);
        schedule.setReportSubmittedAt(LocalDateTime.now());
        schedule.setStatus(TourStatus.COMPLETED);
        tourScheduleRepository.save(schedule);
    }

    private TourScheduleResponse mapToResponse(TourSchedule schedule) {
        return TourScheduleResponse.builder()
                .id(schedule.getId())
                .tourId(schedule.getTour() != null ? schedule.getTour().getId() : null)
                .tourName(schedule.getTour() != null ? schedule.getTour().getTourName() : null)
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .availableSlots(schedule.getAvailableSlots())
                .status(schedule.getStatus() != null ? schedule.getStatus().name() : null)
                .currentProgress(schedule.getCurrentProgress())
                .reportContent(schedule.getReportContent())
                .reportSubmittedAt(schedule.getReportSubmittedAt())
                .build();
    }
}
