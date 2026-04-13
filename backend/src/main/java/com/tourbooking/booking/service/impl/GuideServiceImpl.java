package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.dto.response.ProgressLogResponse;
import com.tourbooking.booking.model.dto.response.TourScheduleResponse;
import com.tourbooking.booking.model.entity.TourSchedule;
import com.tourbooking.booking.model.entity.TourActivityImage;
import com.tourbooking.booking.model.entity.TourProgressLog;
import com.tourbooking.booking.model.entity.enums.TourStatus;
import com.tourbooking.booking.repository.TourActivityImageRepository;
import com.tourbooking.booking.repository.TourProgressLogRepository;
import com.tourbooking.booking.repository.TourScheduleRepository;
import com.tourbooking.booking.service.GuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

    private final TourScheduleRepository tourScheduleRepository;
    private final TourActivityImageRepository tourActivityImageRepository;
    private final TourProgressLogRepository tourProgressLogRepository;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Transactional(readOnly = true)
    public List<TourScheduleResponse> getAssignedTours(Long guideId) {
        log.info("Fetching assigned tours for guide ID: {}", guideId);
        List<TourSchedule> schedules = tourScheduleRepository.findByGuideId(guideId);
        log.info("Found {} schedules for guide ID: {}", schedules.size(), guideId);
        return schedules.stream().map(s -> mapToResponse(s, false)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TourScheduleResponse getAssignedTourDetails(Long guideId, Long scheduleId) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));

        if (!schedule.getGuide().getId().equals(guideId)) {
            throw new RuntimeException("You are not assigned to this tour");
        }

        return mapToResponse(schedule, true);
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
        
        // Save history log
        TourProgressLog logEntry = TourProgressLog.builder()
                .schedule(schedule)
                .content(progress)
                .build();
        tourProgressLogRepository.save(logEntry);

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

        if (photos == null || photos.isEmpty()) return;

        try {
            Path root = Paths.get(UPLOAD_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            for (MultipartFile photo : photos) {
                if (photo.isEmpty()) continue;

                String originalName = photo.getOriginalFilename();
                String fileName = UUID.randomUUID().toString() + "_" + (originalName != null ? originalName : "image.jpg");
                Path targetPath = root.resolve(fileName);

                Files.copy(photo.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                TourActivityImage image = new TourActivityImage();
                image.setSchedule(schedule);
                image.setImageUrl("/uploads/" + fileName);
                tourActivityImageRepository.save(image);
                
                log.info("Saved photo to: {}", targetPath.toString());
            }
        } catch (Exception e) {
            log.error("Failed to upload photos: {}", e.getMessage());
            throw new RuntimeException("Could not store files. Error: " + e.getMessage());
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

    private TourScheduleResponse mapToResponse(TourSchedule schedule, boolean includeFullDetails) {
        try {
            TourScheduleResponse res = TourScheduleResponse.builder()
                    .id(schedule.getId())
                    .tourId(schedule.getTour() != null ? schedule.getTour().getId() : null)
                    .tourName(schedule.getTour() != null ? schedule.getTour().getTourName() : null)
                    .guideId(schedule.getGuide() != null ? schedule.getGuide().getId() : null)
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .availableSlots(schedule.getAvailableSlots())
                    .status(schedule.getStatus() != null ? schedule.getStatus().name() : null)
                    .currentProgress(schedule.getCurrentProgress())
                    .reportContent(schedule.getReportContent())
                    .reportSubmittedAt(schedule.getReportSubmittedAt())
                    .build();

            if (includeFullDetails && schedule.getActivityImages() != null) {
                res.setImageUrls(schedule.getActivityImages().stream()
                        .filter(img -> img != null)
                        .map(TourActivityImage::getImageUrl)
                        .collect(Collectors.toList()));
            } else {
                res.setImageUrls(new java.util.ArrayList<>());
            }

            if (includeFullDetails) {
                try {
                    res.setProgressLogs(tourProgressLogRepository.findByScheduleOrderByCreatedAtDesc(schedule).stream()
                            .filter(l -> l != null)
                            .map(l -> ProgressLogResponse.builder()
                                    .id(l.getId())
                                    .content(l.getContent())
                                    .createdAt(l.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList()));
                } catch (Exception e) {
                    log.warn("Could not load progress logs for schedule {}: {}", schedule.getId(), e.getMessage());
                    res.setProgressLogs(new java.util.ArrayList<>());
                }
            } else {
                res.setProgressLogs(new java.util.ArrayList<>());
            }

            return res;
        } catch (Exception e) {
            log.error("Fatal error mapping schedule {}: {}", schedule.getId(), e.getMessage());
            return TourScheduleResponse.builder().id(schedule.getId()).tourName("Error").build();
        }
    }
}
