package com.tourbooking.booking.service;

import com.tourbooking.booking.model.dto.request.BookingRequest;
import com.tourbooking.booking.model.dto.response.BookingResponse;
import com.tourbooking.booking.model.dto.response.FinancialReportResponse;
import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    List<BookingResponse> getAllBookings();
    List<BookingResponse> getBookingsByUserId(Long userId);
    BookingResponse getBookingById(Long id);
    BookingResponse createBooking(BookingRequest request);
    BookingResponse updateBooking(Long id, BookingRequest request);
    void deleteBooking(Long id);

    List<FinancialReportResponse> getFinancialReport(String start, String end, String type, String status);

    long countActiveBookings();

    BigDecimal getMonthlyRevenue();
}
