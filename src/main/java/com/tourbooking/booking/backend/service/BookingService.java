package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import java.util.List;

public interface BookingService {
    List<BookingResponse> getAllBookings();
    List<BookingResponse> getBookingsByUserId(Long userId);
    BookingResponse getBookingById(Long id);
    BookingResponse createBooking(BookingRequest request);
    BookingResponse updateBooking(Long id, BookingRequest request);
    void deleteBooking(Long id);
}
