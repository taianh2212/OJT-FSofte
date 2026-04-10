package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.exception.AppException;
import com.tourbooking.booking.exception.ErrorCode;
import com.tourbooking.booking.mapper.BookingMapper;
import com.tourbooking.booking.model.dto.request.BookingRequest;
import com.tourbooking.booking.model.dto.response.BookingResponse;
import com.tourbooking.booking.model.entity.Booking;
import com.tourbooking.booking.model.entity.Payment;
import com.tourbooking.booking.model.entity.TourSchedule;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.model.entity.enums.BookingStatus;
import com.tourbooking.booking.repository.BookingRepository;
import com.tourbooking.booking.repository.PaymentRepository;
import com.tourbooking.booking.repository.TourScheduleRepository;
import com.tourbooking.booking.repository.DiscountRepository;
import com.tourbooking.booking.model.entity.Discount;
import com.tourbooking.booking.model.entity.enums.DiscountType;
import com.tourbooking.booking.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.repository.UserRepository;
import java.time.LocalDateTime;
import com.tourbooking.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.tourbooking.booking.model.dto.response.FinancialReportResponse;
import com.tourbooking.booking.model.entity.enums.BookingStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final DiscountRepository discountRepository;
    private final PaymentRepository paymentRepository;

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

        // UC15 ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ FIX Ãƒâ€žÃ‚ÂÃƒÆ’Ã…Â¡NG CHÃƒÂ¡Ã‚Â»Ã¢â‚¬â€œ
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

            // check slot schedule mÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi
            if (newSchedule.getAvailableSlots() < people) {
                throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
            }

            // trÃƒÂ¡Ã‚ÂºÃ‚Â£ slot schedule cÃƒâ€¦Ã‚Â©
            oldSchedule.setAvailableSlots(oldSchedule.getAvailableSlots() + people);

            // trÃƒÂ¡Ã‚Â»Ã‚Â« slot schedule mÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi
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

            // nÃƒÂ¡Ã‚ÂºÃ‚Â¿u tÃƒâ€žÃ†â€™ng ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi
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

        tourScheduleRepository.save(schedule); // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ FIX
    }

    @Transactional
    public void requestRefund(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // TODO: check payment SUCCESS (nÃƒÂ¡Ã‚ÂºÃ‚Â¿u cÃƒÆ’Ã‚Â³ PaymentService)

        booking.setStatus(BookingStatus.REFUND_REQUESTED);
    }

    @Override
    public List<FinancialReportResponse> getFinancialReport(String start, String end, String type, String status) {
        LocalDateTime startDateTime = LocalDate.parse(start).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(end).atTime(23, 59, 59);

        log.info("Generating financial report from {} to {}, type: {}, status: {}", start, end, type, status);

        // LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ payments trong khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng thÃƒÂ¡Ã‚Â»Ã‚Âi gian
        List<Payment> allPayments = paymentRepository.findAll();
        log.info("Total payments in DB: {}", allPayments.size());

        // LÃƒÂ¡Ã‚Â»Ã‚Âc payments theo ngÃƒÆ’Ã‚Â y thanh toÃƒÆ’Ã‚Â¡n (paymentDate hoÃƒÂ¡Ã‚ÂºÃ‚Â·c createdAt)
        List<Payment> filteredPayments = allPayments.stream()
                .filter(p -> {
                    LocalDateTime pDate = p.getPaymentDate() != null ? p.getPaymentDate() : p.getCreatedAt();
                    if (pDate == null) return false;
                    return (pDate.isEqual(startDateTime) || pDate.isAfter(startDateTime)) &&
                           (pDate.isEqual(endDateTime) || pDate.isBefore(endDateTime));
                })
                .filter(p -> {
                    // NÃƒÂ¡Ã‚ÂºÃ‚Â¿u filter status lÃƒÆ’Ã‚Â  "all" hoÃƒÂ¡Ã‚ÂºÃ‚Â·c khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³, lÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ payment SUCCESS
                    if (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)) {
                        return p.getStatus() == PaymentStatus.SUCCESS;
                    }
                    // NÃƒÂ¡Ã‚ÂºÃ‚Â¿u filter lÃƒÆ’Ã‚Â  SUCCESS hoÃƒÂ¡Ã‚ÂºÃ‚Â·c COMPLETED ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ lÃƒÂ¡Ã‚ÂºÃ‚Â¥y payment SUCCESS
                    if ("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        return p.getStatus() == PaymentStatus.SUCCESS;
                    }
                    // CÃƒÆ’Ã‚Â¡c trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âng hÃƒÂ¡Ã‚Â»Ã‚Â£p khÃƒÆ’Ã‚Â¡c (CANCELLED, CONFIRMED...) ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ revenue tÃƒÂ¡Ã‚Â»Ã‚Â« payment
                    return false;
                })
                .toList();

        log.info("Filtered payments (SUCCESS) count: {}", filteredPayments.size());

        // LÃƒÂ¡Ã‚ÂºÃ‚Â¥y bookings bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ CANCELLED trong khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng thÃƒÂ¡Ã‚Â»Ã‚Âi gian (Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â­nh cancellation rate)
        List<Booking> cancelledBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .filter(b -> {
                    LocalDateTime bDate = b.getCreatedAt() != null ? b.getCreatedAt() : b.getUpdatedAt();
                    if (bDate == null) return false;
                    return (bDate.isEqual(startDateTime) || bDate.isAfter(startDateTime)) &&
                           (bDate.isEqual(endDateTime) || bDate.isBefore(endDateTime));
                })
                .toList();

        // Formatter theo loÃƒÂ¡Ã‚ÂºÃ‚Â¡i bÃƒÆ’Ã‚Â¡o cÃƒÆ’Ã‚Â¡o
        DateTimeFormatter formatter = switch (type.toLowerCase()) {
            case "weekly"  -> DateTimeFormatter.ofPattern("yyyy-'W'ww", Locale.getDefault());
            case "monthly" -> DateTimeFormatter.ofPattern("yyyy-MM");
            case "yearly"  -> DateTimeFormatter.ofPattern("yyyy");
            default        -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
        };

        // Group payments theo period
        Map<String, List<Payment>> groupedPayments = filteredPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            LocalDateTime date = p.getPaymentDate() != null ? p.getPaymentDate() : p.getCreatedAt();
                            return date != null ? date.format(formatter) : "Unknown";
                        },
                        TreeMap::new,
                        Collectors.toList()
                ));

        // Group cancellations theo period
        Map<String, Long> cancelledByPeriod = cancelledBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> {
                            LocalDateTime date = b.getCreatedAt() != null ? b.getCreatedAt() : b.getUpdatedAt();
                            return date != null ? date.format(formatter) : "Unknown";
                        },
                        Collectors.counting()
                ));

        // NÃƒÂ¡Ã‚ÂºÃ‚Â¿u khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ payment nÃƒÆ’Ã‚Â o, trÃƒÂ¡Ã‚ÂºÃ‚Â£ vÃƒÂ¡Ã‚Â»Ã‚Â danh sÃƒÆ’Ã‚Â¡ch rÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ng vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi log warning
        if (groupedPayments.isEmpty()) {
            log.warn("No SUCCESS payments found in range {} - {}", start, end);
        }

        return groupedPayments.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<Payment> periodPayments = entry.getValue();

                    long bookingCount = periodPayments.size();

                    // Revenue = tÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng Payment.amount cÃƒÂ¡Ã‚Â»Ã‚Â§a cÃƒÆ’Ã‚Â¡c payment SUCCESS
                    BigDecimal revenue = periodPayments.stream()
                            .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long cancellations = cancelledByPeriod.getOrDefault(period, 0L);

                    BigDecimal averageValue = bookingCount > 0
                            ? revenue.divide(BigDecimal.valueOf(bookingCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    log.info("Period: {}, bookings: {}, revenue: {}, cancellations: {}", period, bookingCount, revenue, cancellations);

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

    @Override
    public long countActiveBookings() {
        long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        return confirmed + pending;
    }

    @Override
    public BigDecimal getMonthlyRevenue() {
        YearMonth current = YearMonth.now();
        LocalDateTime start = current.atDay(1).atStartOfDay();
        LocalDateTime end = current.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal revenue = paymentRepository.sumAmountByStatusAndDateBetween(
                com.tourbooking.booking.model.entity.enums.PaymentStatus.SUCCESS, start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
