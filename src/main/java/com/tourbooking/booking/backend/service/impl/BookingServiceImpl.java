package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.BookingMapper;
import com.tourbooking.booking.backend.model.dto.request.BookingRequest;
import com.tourbooking.booking.backend.model.dto.response.BookingResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.Payment;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.PaymentRepository;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import com.tourbooking.booking.backend.repository.DiscountRepository;
import com.tourbooking.booking.backend.model.entity.Discount;
import com.tourbooking.booking.backend.model.entity.enums.DiscountType;
import com.tourbooking.booking.backend.model.entity.enums.PaymentStatus;
import com.tourbooking.booking.backend.repository.UserRepository;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

        // Lấy tất cả payments trong khoảng thời gian
        List<Payment> allPayments = paymentRepository.findAll();
        log.info("Total payments in DB: {}", allPayments.size());

        // Lọc payments theo ngày thanh toán (paymentDate hoặc createdAt)
        List<Payment> filteredPayments = allPayments.stream()
                .filter(p -> {
                    LocalDateTime pDate = p.getPaymentDate() != null ? p.getPaymentDate() : p.getCreatedAt();
                    if (pDate == null) return false;
                    return (pDate.isEqual(startDateTime) || pDate.isAfter(startDateTime)) &&
                           (pDate.isEqual(endDateTime) || pDate.isBefore(endDateTime));
                })
                .filter(p -> {
                    // Nếu filter status là "all" hoặc không có, lấy tất cả payment SUCCESS
                    if (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)) {
                        return p.getStatus() == PaymentStatus.SUCCESS;
                    }
                    // Nếu filter là SUCCESS hoặc COMPLETED → lấy payment SUCCESS
                    if ("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        return p.getStatus() == PaymentStatus.SUCCESS;
                    }
                    // Các trường hợp khác (CANCELLED, CONFIRMED...) → không có revenue từ payment
                    return false;
                })
                .toList();

        log.info("Filtered payments (SUCCESS) count: {}", filteredPayments.size());

        // Lấy bookings bị CANCELLED trong khoảng thời gian (để tính cancellation rate)
        List<Booking> cancelledBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .filter(b -> {
                    LocalDateTime bDate = b.getCreatedAt() != null ? b.getCreatedAt() : b.getUpdatedAt();
                    if (bDate == null) return false;
                    return (bDate.isEqual(startDateTime) || bDate.isAfter(startDateTime)) &&
                           (bDate.isEqual(endDateTime) || bDate.isBefore(endDateTime));
                })
                .toList();

        // Formatter theo loại báo cáo
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

        // Nếu không có payment nào, trả về danh sách rỗng với log warning
        if (groupedPayments.isEmpty()) {
            log.warn("No SUCCESS payments found in range {} - {}", start, end);
        }

        return groupedPayments.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<Payment> periodPayments = entry.getValue();

                    long bookingCount = periodPayments.size();

                    // Revenue = tổng Payment.amount của các payment SUCCESS
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
                com.tourbooking.booking.backend.model.entity.enums.PaymentStatus.SUCCESS, start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
