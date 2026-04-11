package com.tourbooking.booking.service;

import com.tourbooking.booking.model.dto.response.BookingResponse;
import com.tourbooking.booking.model.dto.response.RefundResponse;
import com.tourbooking.booking.model.dto.response.UserResponse;
import com.tourbooking.booking.model.entity.enums.RefundStatus;
import java.util.List;

public interface StaffService {
    void confirmBooking(Long bookingId);
    void assignGuide(Long scheduleId, Long guideId);
    List<RefundResponse> listRefundRequests();
    void processRefund(Long refundId, RefundStatus status, String staffNote);
    List<UserResponse> listGuides();
    List<BookingResponse> listBookings(String status);
    List<com.tourbooking.booking.model.dto.response.TourScheduleResponse> listSchedules();
}
