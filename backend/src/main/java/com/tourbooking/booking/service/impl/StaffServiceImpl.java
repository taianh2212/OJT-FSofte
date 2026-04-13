package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.dto.response.BookingResponse;
import com.tourbooking.booking.model.dto.response.RefundResponse;
import com.tourbooking.booking.model.dto.response.UserResponse;
import com.tourbooking.booking.model.entity.Booking;
import com.tourbooking.booking.model.entity.RefundRequest;
import com.tourbooking.booking.model.entity.TourSchedule;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.model.entity.enums.BookingStatus;
import com.tourbooking.booking.model.entity.enums.RefundStatus;
import com.tourbooking.booking.model.entity.enums.UserRole;
import com.tourbooking.booking.repository.BookingRepository;
import com.tourbooking.booking.repository.RefundRequestRepository;
import com.tourbooking.booking.repository.TourProgressLogRepository;
import com.tourbooking.booking.repository.TourScheduleRepository;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.service.StaffService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final BookingRepository bookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final UserRepository userRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final TourProgressLogRepository tourProgressLogRepository;

    @Override
    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void assignGuide(Long scheduleId, Long guideId) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));
        
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
                
        if (guide.getRole() != UserRole.GUIDE) {
            throw new RuntimeException("User selected is not a GUIDE");
        }
        
        schedule.setGuide(guide);
        tourScheduleRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> listRefundRequests() {
        return refundRequestRepository.findAll().stream()
                .map(req -> RefundResponse.builder()
                        .id(req.getId())
                        .bookingId(req.getBooking() != null ? req.getBooking().getId() : null)
                        .amount(req.getAmount())
                        .reason(req.getReason())
                        .status(req.getStatus().name())
                        .staffNote(req.getStaffNote())
                        .processedAt(req.getProcessedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processRefund(Long refundId, RefundStatus status, String staffNote) {
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));
        refund.setStatus(status);
        refund.setStaffNote(staffNote);
        refund.setProcessedAt(LocalDateTime.now());
        refundRequestRepository.save(refund);
    }

    @Override
    public List<UserResponse> listGuides() {
        return userRepository.findByRole(UserRole.GUIDE).stream()
                .map(u -> {
                    UserResponse res = new UserResponse();
                    res.setId(u.getId());
                    res.setFullName(u.getFullName());
                    res.setEmail(u.getEmail());
                    res.setRole(u.getRole());
                    res.setIsActive(u.getIsActive());
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listBookings(String status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByStatus(BookingStatus.valueOf(status.toUpperCase()));
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .map(b -> {
                    BookingResponse res = new BookingResponse();
                    res.setId(b.getId());
                    res.setUserId(b.getUser() != null ? b.getUser().getId() : null);
                    res.setUserFullName(b.getUser() != null ? b.getUser().getFullName() : "Guest");
                    res.setScheduleId(b.getSchedule() != null ? b.getSchedule().getId() : null);
                    res.setTourName(b.getSchedule() != null && b.getSchedule().getTour() != null ? 
                                    b.getSchedule().getTour().getTourName() : "N/A");
                    res.setBookingDate(b.getBookingDate());
                    res.setNumberOfPeople(b.getNumberOfPeople());
                    res.setTotalPrice(b.getTotalPrice());
                    res.setStatus(b.getStatus());
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public com.tourbooking.booking.model.dto.response.TourScheduleResponse getScheduleDetails(Long id) {
        TourSchedule schedule = tourScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour schedule not found"));
        return mapToResponse(schedule, true); // Include full logs
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<com.tourbooking.booking.model.dto.response.TourScheduleResponse> listSchedules() {
        return tourScheduleRepository.findAll().stream()
                .map(s -> mapToResponse(s, false)) // Minimal logs for list
                .collect(Collectors.toList());
    }

    private com.tourbooking.booking.model.dto.response.TourScheduleResponse mapToResponse(TourSchedule schedule, boolean includeFullLogs) {
        try {
            var res = com.tourbooking.booking.model.dto.response.TourScheduleResponse.builder()
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

            if (schedule.getActivityImages() != null) {
                res.setImageUrls(schedule.getActivityImages().stream()
                        .filter(img -> img != null)
                        .map(img -> img.getImageUrl())
                        .collect(Collectors.toList()));
            } else {
                res.setImageUrls(new java.util.ArrayList<>());
            }

            if (includeFullLogs) {
                res.setProgressLogs(tourProgressLogRepository.findByScheduleOrderByCreatedAtDesc(schedule).stream()
                        .filter(l -> l != null)
                        .map(l -> com.tourbooking.booking.model.dto.response.ProgressLogResponse.builder()
                                .id(l.getId())
                                .content(l.getContent())
                                .createdAt(l.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()));
            } else {
                res.setProgressLogs(new java.util.ArrayList<>());
            }
            
            return res;
        } catch (Exception e) {
            return com.tourbooking.booking.model.dto.response.TourScheduleResponse.builder()
                    .id(schedule.getId())
                    .tourName("Error Loading")
                    .build();
        }
    }
}
