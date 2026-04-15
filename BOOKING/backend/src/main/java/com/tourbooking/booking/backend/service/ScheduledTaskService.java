package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.config.OpenTripMapProperties;
import com.tourbooking.booking.backend.model.dto.response.OpenTripMapFetchSummaryResponse;
import com.tourbooking.booking.backend.model.entity.Booking;
import com.tourbooking.booking.backend.model.entity.TourSchedule;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import com.tourbooking.booking.backend.model.entity.enums.TourStatus;
import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import com.tourbooking.booking.backend.repository.BookingRepository;
import com.tourbooking.booking.backend.repository.TourScheduleRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service chứa tất cả các tác vụ tự động (Scheduled Jobs):
 * - UC46: Tự động cập nhật chỗ trống của TourSchedule
 * - UC47: Tự động hủy Booking chưa thanh toán quá 24h
 * - UC48: Gửi email thông báo khi hủy booking (tích hợp trong UC47)
 * - UC49: Tự động cộng điểm loyal cho booking COMPLETED
 * - UC50: Tạo và gửi báo cáo tháng cho ADMIN
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final BookingRepository bookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final LoyaltyService loyaltyService;
    private final OpenTripMapService openTripMapService;
    private final OpenTripMapProperties openTripMapProperties;
    private final TourRepository tourRepository;

    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void autoUpdateSlots() {
        // Lấy cả OPEN và FULL để kiểm tra chuyển trạng thái hai chiều
        List<TourSchedule> schedules = tourScheduleRepository.findAll().stream()
                .filter(s -> s.getStatus() == TourStatus.OPEN || s.getStatus() == TourStatus.FULL)
                .toList();

        if (schedules.isEmpty()) return;

        int updated = 0;
        for (TourSchedule schedule : schedules) {
            TourStatus oldStatus = schedule.getStatus();

            // Kiểm tra schedule đã quá ngày khởi hành
            if (schedule.getStartDate() != null && schedule.getStartDate().isBefore(java.time.LocalDate.now())) {
                schedule.setStatus(TourStatus.COMPLETED);
                tourScheduleRepository.save(schedule);
                updated++;
                continue;
            }

            // OPEN → FULL khi hết chỗ
            if (oldStatus == TourStatus.OPEN
                    && schedule.getAvailableSlots() != null
                    && schedule.getAvailableSlots() <= 0) {
                schedule.setStatus(TourStatus.FULL);
                tourScheduleRepository.save(schedule);
                updated++;
            }

            // FULL → OPEN khi có chỗ trở lại (do hủy booking)
            if (oldStatus == TourStatus.FULL
                    && schedule.getAvailableSlots() != null
                    && schedule.getAvailableSlots() > 0) {
                schedule.setStatus(TourStatus.OPEN);
                tourScheduleRepository.save(schedule);
                updated++;
            }
        }

        if (updated > 0) {
            log.info("[UC46] Cập nhật trạng thái cho {} TourSchedule.", updated);
        }
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void autoCancelUnpaidBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Booking> unpaidBookings = bookingRepository.findPendingUnpaidBefore(cutoff);

        if (unpaidBookings.isEmpty()) return;

        for (Booking booking : unpaidBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            TourSchedule schedule = booking.getSchedule();
            if (schedule != null && booking.getNumberOfPeople() != null) {
                int currentSlots = schedule.getAvailableSlots() != null ? schedule.getAvailableSlots() : 0;
                schedule.setAvailableSlots(currentSlots + booking.getNumberOfPeople());
                if (schedule.getStatus() == TourStatus.FULL && schedule.getAvailableSlots() > 0) {
                    schedule.setStatus(TourStatus.OPEN);
                }
                tourScheduleRepository.save(schedule);
            }

            User customer = booking.getUser();
            if (customer != null && customer.getEmail() != null) {
                mailService.sendBookingCancelledEmail(
                        customer.getEmail(),
                        customer.getFullName() != null ? customer.getFullName() : "Quý khách",
                        booking.getId(),
                        booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO
                );
            }
        }

        log.info("[UC47] Đã hủy {} booking PENDING quá hạn (> 24h chưa thanh toán).", unpaidBookings.size());
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoUpdateLoyaltyPoints() {
        log.info("[UC49] Bắt đầu cộng điểm loyalty cho booking COMPLETED...");

        // Lấy tất cả booking COMPLETED mà chưa có discountCode bắt đầu bằng "LOYALTY_AWARDED"
        // (dùng discountCode không null check đơn giản - hoặc ta dùng flag riêng)
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);

        int awarded = 0;
        for (Booking booking : completedBookings) {
            // Skip nếu đã cộng điểm rồi (đánh dấu bằng prefix trong discountCode)
            if (booking.getDiscountCode() != null && booking.getDiscountCode().startsWith("LOYALTY_AWARDED")) {
                continue;
            }
            if (booking.getUser() == null || booking.getTotalPrice() == null) {
                continue;
            }

            // Quy đổi: mỗi 100,000 VND = 1 điểm
            int points = booking.getTotalPrice().intValue() / 100000;
            if (points > 0) {
                try {
                    loyaltyService.addPoint(booking.getUser().getId(), points);

                    // Đánh dấu đã cộng điểm để tránh cộng trùng
                    String existingCode = booking.getDiscountCode();
                    booking.setDiscountCode(
                        "LOYALTY_AWARDED" + (existingCode != null ? "|" + existingCode : "")
                    );
                    bookingRepository.save(booking);
                    awarded++;
                } catch (Exception e) {
                    log.error("[UC49] Lỗi cộng điểm cho booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }

        log.info("[UC49] Đã cộng điểm cho {} booking COMPLETED.", awarded);
    }

    @Scheduled(cron = "0 0 8 1 * *")
    @Transactional
    public void generateMonthlyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime firstDayOfThisMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        String monthYear = firstDayOfLastMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        log.info("[UC50] Bắt đầu tạo báo cáo tháng {}...", monthYear);

        long totalBookings = bookingRepository.findAllInPeriod(firstDayOfLastMonth, firstDayOfThisMonth).size();
        long confirmed    = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.CONFIRMED, firstDayOfLastMonth, firstDayOfThisMonth);
        long cancelled    = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.CANCELLED, firstDayOfLastMonth, firstDayOfThisMonth);
        long completed    = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.COMPLETED, firstDayOfLastMonth, firstDayOfThisMonth);
        BigDecimal revenue = bookingRepository.sumRevenueConfirmedBetween(firstDayOfLastMonth, firstDayOfThisMonth);

        String reportContent = buildReportContent(monthYear, totalBookings, confirmed, cancelled, completed, revenue);

        List<User> allUsers = userRepository.findAll();
        allUsers.stream()
                .filter(u -> u.getRole() == UserRole.ADMIN && u.getEmail() != null && Boolean.TRUE.equals(u.getIsActive()))
                .forEach(admin -> {
                    mailService.sendMonthlyReportEmail(admin.getEmail(), reportContent, monthYear);
                    log.info("[UC50] Đã gửi báo cáo tháng {} cho admin: {}", monthYear, admin.getEmail());
                });
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshOpenTripMapContent() {
        List<String> cities = openTripMapProperties.getSchedulerCities();
        if (cities == null || cities.isEmpty()) {
            log.debug("[OpenTripMap Scheduler] No cities configured, skipping run.");
            return;
        }
        int limit = Math.min(openTripMapProperties.getSchedulerMaxCitiesPerRun(), cities.size());
        int processed = 0;
        for (String city : cities) {
            if (processed >= limit) {
                break;
            }
            long existing = tourRepository.countBySourceAndStartLocationIgnoreCase("OPENTRIPMAP", city);
            if (existing > 50) {
                log.info("[OpenTripMap Scheduler] {} already has {} tours, skipping", city, existing);
                continue;
            }
            try {
                OpenTripMapFetchSummaryResponse summary = openTripMapService.fetchAndSaveAll(city);
                log.info("[OpenTripMap Scheduler] {} inserted={} skipped={}", city, summary.getInsertedCount(), summary.getSkippedCount());
            } catch (Exception ex) {
                log.error("[OpenTripMap Scheduler] Failed to refresh {}: {}", city, ex.getMessage());
            }
            processed++;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String buildReportContent(String monthYear, long total, long confirmed,
                                       long cancelled, long completed, BigDecimal revenue) {
        return "========================================\n" +
               "   BÁO CÁO KINH DOANH THÁNG " + monthYear + "\n" +
               "========================================\n\n" +
               "THỐNG KÊ BOOKING:\n" +
               "  - Tổng booking trong tháng  : " + total     + "\n" +
               "  - Đã xác nhận (CONFIRMED)   : " + confirmed + "\n" +
               "  - Đã hoàn thành (COMPLETED) : " + completed + "\n" +
               "  - Đã hủy (CANCELLED)        : " + cancelled + "\n\n" +
               "DOANH THU:\n" +
               "  - Tổng doanh thu tháng      : " + String.format("%,.0f", revenue) + " VND\n\n" +
               "========================================\n" +
               "Báo cáo được tạo tự động bởi hệ thống TourBooking.\n" +
               "Ngày tạo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n";
    }
}
