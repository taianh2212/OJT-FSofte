package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.BookingMapper;
import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
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

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // UC13
        if (schedule.getAvailableSlots() < request.getNumberOfPeople()) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // UC14
        var price = schedule.getTour().getPrice();
        var totalPrice = price.multiply(
                java.math.BigDecimal.valueOf(request.getNumberOfPeople()));

        // UC15 ✅ FIX ĐÚNG CHỖ
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            totalPrice = totalPrice.multiply(java.math.BigDecimal.valueOf(0.9));
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setNumberOfPeople(request.getNumberOfPeople());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(java.time.LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // update slot
        schedule.setAvailableSlots(
                schedule.getAvailableSlots() - request.getNumberOfPeople());

        tourScheduleRepository.save(schedule);

        return BookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (existingBooking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        if (request.getUserId() != null && !request.getUserId().equals(existingBooking.getUser().getId())) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            existingBooking.setUser(user);
        }

        if (request.getScheduleId() != null &&
                !request.getScheduleId().equals(existingBooking.getSchedule().getId())) {

            TourSchedule oldSchedule = existingBooking.getSchedule();

            TourSchedule newSchedule = tourScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

            int people = existingBooking.getNumberOfPeople();

            // check slot schedule mới
            if (newSchedule.getAvailableSlots() < people) {
                throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
            }

            // trả slot schedule cũ
            oldSchedule.setAvailableSlots(oldSchedule.getAvailableSlots() + people);

            // trừ slot schedule mới
            newSchedule.setAvailableSlots(newSchedule.getAvailableSlots() - people);

            tourScheduleRepository.save(oldSchedule);
            tourScheduleRepository.save(newSchedule);

            existingBooking.setSchedule(newSchedule);
        }
        // handle change numberOfPeople
        if (request.getNumberOfPeople() != null) {

            int oldValue = existingBooking.getNumberOfPeople();
            int newValue = request.getNumberOfPeople();

            int diff = newValue - oldValue;

            TourSchedule schedule = existingBooking.getSchedule();

            // nếu tăng người
            if (diff > 0 && schedule.getAvailableSlots() < diff) {
                throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
            }

            // update slot
            schedule.setAvailableSlots(schedule.getAvailableSlots() - diff);
            tourScheduleRepository.save(schedule);
        }
        BookingMapper.updateEntityFromRequest(existingBooking, request);

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

    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        TourSchedule schedule = booking.getSchedule();
        schedule.setAvailableSlots(
                schedule.getAvailableSlots() + booking.getNumberOfPeople());

        tourScheduleRepository.save(schedule); // ✅ FIX
    }

    @Transactional
    public void requestRefund(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // TODO: check payment SUCCESS (nếu có PaymentService)

        booking.setStatus(BookingStatus.REFUND_REQUESTED);
    }
}