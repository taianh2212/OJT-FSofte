package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.BookingMapper;
import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Booking booking = BookingMapper.toEntity(request);
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND)); // Or a more specific TOUR_SCHEDULE_NOT_FOUND if needed
        
        booking.setUser(user);
        booking.setSchedule(schedule);
        
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        BookingMapper.updateEntityFromRequest(existingBooking, request);

        if (request.getUserId() != null && !request.getUserId().equals(existingBooking.getUser().getId())) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            existingBooking.setUser(user);
        }

        if (request.getScheduleId() != null && !request.getScheduleId().equals(existingBooking.getSchedule().getId())) {
            TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
            existingBooking.setSchedule(schedule);
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return BookingMapper.toResponse(updatedBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
        bookingRepository.deleteById(id);
    }
}
