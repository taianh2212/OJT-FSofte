package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.response.RefundResponse;
import com.tourbooking.booking.backend.model.entity.enums.RefundStatus;
import java.util.List;

public interface StaffService {
    void confirmBooking(Long bookingId);
    void assignGuide(Long scheduleId, Long guideId);
    List<RefundResponse> listRefundRequests();
    void processRefund(Long refundId, RefundStatus status, String staffNote);
}
