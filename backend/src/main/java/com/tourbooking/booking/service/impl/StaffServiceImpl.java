package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.dto.response.RefundResponse;
import com.tourbooking.booking.model.entity.Booking;
import com.tourbooking.booking.model.entity.RefundRequest;
import com.tourbooking.booking.model.entity.TourSchedule;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.model.entity.enums.BookingStatus;
import com.tourbooking.booking.model.entity.enums.RefundStatus;
import com.tourbooking.booking.model.entity.enums.UserRole;
import com.tourbooking.booking.repository.BookingRepository;
import com.tourbooking.booking.repository.RefundRequestRepository;
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
}
