package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import com.tourbooking.booking.backend.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.tourbooking.booking.backend.model.dto.response.FinancialReportResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> getAllBookings() {
        return ApiResponse.<List<BookingResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved all bookings")
                .data(bookingService.getAllBookings())
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
        return ApiResponse.<List<BookingResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved bookings for user: " + userId)
                .data(bookingService.getBookingsByUserId(userId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved booking details")
                .data(bookingService.getBookingById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Booking created successfully")
                .data(bookingService.createBooking(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BookingResponse> updateBooking(@PathVariable Long id, @RequestBody BookingRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Booking updated successfully")
                .data(bookingService.updateBooking(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Booking deleted successfully")
                .build();
    }
}
