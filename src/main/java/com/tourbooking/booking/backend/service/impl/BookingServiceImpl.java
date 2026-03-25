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
import com.tourbooking.booking.backend.repository.DiscountRepository;
import com.tourbooking.booking.backend.model.entity.Discount;
import com.tourbooking.booking.backend.model.entity.enums.DiscountType;
import com.tourbooking.booking.backend.repository.UserRepository;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import com.tourbooking.booking.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.tourbooking.booking.backend.model.dto.response.FinancialReportResponse;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final DiscountRepository discountRepository;

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

        // Handle Discount
        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            Discount discount = discountRepository.findByCode(request.getDiscountCode())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND)); // Or a better error code

            // Basic validation (duplicated from service for safety)
            if (discount.getIsActive() && 
                (discount.getStartDate() == null || !LocalDateTime.now().isBefore(discount.getStartDate())) &&
                (discount.getEndDate() == null || !LocalDateTime.now().isAfter(discount.getEndDate())) &&
                (discount.getUsageLimit() == null || discount.getCurrentUsage() < discount.getUsageLimit())) {
                
                BigDecimal discountAmount = BigDecimal.ZERO;
                if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                    discountAmount = booking.getTotalPrice().multiply(discount.getValue()).divide(new BigDecimal(100), RoundingMode.HALF_UP);
                } else {
                    discountAmount = discount.getValue();
                }
                
                booking.setDiscountAmount(discountAmount);
                booking.setDiscountCode(discount.getCode());
                booking.setTotalPrice(booking.getTotalPrice().subtract(discountAmount));
                
                // Update usage
                discount.setCurrentUsage(discount.getCurrentUsage() + 1);
                discountRepository.save(discount);
            }
        }
        
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

    @Override
    public List<FinancialReportResponse> getFinancialReport(String start, String end, String type, String status) {
        LocalDateTime startDateTime = LocalDate.parse(start).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(end).atTime(23, 59, 59);

        log.info("Generating financial report from {} to {}, type: {}, status: {}", start, end, type, status);
        List<Booking> allBookings = bookingRepository.findAll();
        log.info("Total bookings in DB: {}", allBookings.size());
        
        List<Booking> filteredBookings = allBookings.stream()
                .filter(b -> {
                    LocalDateTime bDate = b.getCreatedAt();
                    if (bDate == null) bDate = b.getUpdatedAt();
                    
                    if (bDate == null) {
                        log.debug("Booking ID {} has no date, including as 'Unknown'", b.getId());
                        return true; 
                    }
                    return (bDate.isAfter(startDateTime) || bDate.isEqual(startDateTime)) && 
                           (bDate.isBefore(endDateTime) || bDate.isEqual(endDateTime));
                })
                .filter(b -> {
                    if (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)) return true;
                    if (b.getStatus() == null) return false;
                    return b.getStatus().name().equalsIgnoreCase(status);
                })
                .toList();
        
        log.info("Filtered bookings count: {}", filteredBookings.size());

        DateTimeFormatter formatter = switch (type.toLowerCase()) {
            case "weekly" -> DateTimeFormatter.ofPattern("yyyy-'W'w", Locale.getDefault());
            case "monthly" -> DateTimeFormatter.ofPattern("yyyy-MM");
            case "yearly" -> DateTimeFormatter.ofPattern("yyyy");
            default -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
        };

        Map<String, List<Booking>> groupedData = filteredBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> {
                            LocalDateTime date = b.getCreatedAt() != null ? b.getCreatedAt() : b.getUpdatedAt();
                            return date != null ? date.format(formatter) : "Unknown";
                        },
                        TreeMap::new,
                        Collectors.toList()
                ));

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<Booking> periodBookings = entry.getValue();
                    
                    long bookingCount = periodBookings.size();
                    
                    // Business Logic: Revenue is based on Amount field, only for SUCCESS or COMPLETED bookings
                    // or better yet, if a Payment exists and is SUCCESS.
                    BigDecimal revenue = periodBookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.SUCCESS || b.getStatus() == BookingStatus.COMPLETED)
                            .map(b -> b.getTotalPrice() != null ? b.getTotalPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    long cancellations = periodBookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                            .count();
                    
                    BigDecimal averageValue = (bookingCount - cancellations) > 0 ? 
                            revenue.divide(BigDecimal.valueOf(bookingCount - cancellations), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                    return FinancialReportResponse.builder()
                            .period(period)
                            .bookingCount(bookingCount)
                            .revenue(revenue)
                            .averageValue(averageValue)
                            .cancellations(cancellations)
                            .build();
                })
                .sorted((a, b) -> a.getPeriod().compareTo(b.getPeriod()))
                .toList();
    }
}
