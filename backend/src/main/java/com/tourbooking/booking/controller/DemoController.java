package com.tourbooking.booking.controller;

import com.tourbooking.booking.model.dto.response.ApiResponse;
import com.tourbooking.booking.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller dành riêng cho việc DEMO các UC tự động (UC46-UC50).
 * Cho phép trigger thủ công từng UC mà không cần chờ lịch chạy.
 * 
 * LƯU Ý: Nên xóa hoặc bảo vệ controller này trước khi deploy production.
 */
@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final ScheduledTaskService scheduledTaskService;

    /**
     * UC46: Tự động cập nhật chỗ trống (slot) cho TourSchedule.
     * - OPEN → FULL khi hết slot
     * - FULL → OPEN khi có slot trở lại
     * - Đánh COMPLETED cho schedule quá ngày
     */
    @PostMapping("/uc46")
    public ApiResponse<Map<String, String>> triggerUC46() {
        scheduledTaskService.autoUpdateSlots();
        return ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("UC46 - Auto Update Slots executed successfully")
                .data(Map.of(
                    "uc", "UC46",
                    "name", "Auto Update Slots",
                    "description", "Cập nhật trạng thái OPEN/FULL/COMPLETED cho các TourSchedule",
                    "status", "DONE"
                ))
                .build();
    }

    /**
     * UC47: Tự động hủy booking PENDING quá 24h chưa thanh toán.
     * - Chuyển status → CANCELLED
     * - Trả lại slot cho TourSchedule
     * - Gửi email thông báo cho khách hàng (UC48)
     */
    @PostMapping("/uc47")
    public ApiResponse<Map<String, String>> triggerUC47() {
        scheduledTaskService.autoCancelUnpaidBookings();
        return ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("UC47 - Auto Cancel Unpaid Bookings executed successfully")
                .data(Map.of(
                    "uc", "UC47",
                    "name", "Auto Cancel Unpaid Booking",
                    "description", "Hủy các booking PENDING > 24h chưa thanh toán, trả slot và gửi email",
                    "status", "DONE"
                ))
                .build();
    }

    /**
     * UC49: Tự động cộng điểm loyalty cho booking COMPLETED.
     * - Mỗi 100,000 VND = 1 điểm
     * - Tạo LoyaltyPoint record nếu user chưa có
     * - Đánh dấu đã cộng để tránh cộng trùng
     */
    @PostMapping("/uc49")
    public ApiResponse<Map<String, String>> triggerUC49() {
        scheduledTaskService.autoUpdateLoyaltyPoints();
        return ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("UC49 - Auto Update Loyalty Points executed successfully")
                .data(Map.of(
                    "uc", "UC49",
                    "name", "Auto Update Loyalty Points",
                    "description", "Cộng điểm loyalty cho các booking COMPLETED (1 điểm / 100k VND)",
                    "status", "DONE"
                ))
                .build();
    }

    /**
     * UC50: Tạo báo cáo tháng trước và gửi email cho tất cả Admin.
     * - Thống kê: tổng booking, confirmed, cancelled, completed
     * - Tính doanh thu
     * - Gửi email cho tất cả user có role ADMIN
     */
    @PostMapping("/uc50")
    public ApiResponse<Map<String, String>> triggerUC50() {
        scheduledTaskService.generateMonthlyReport();
        return ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("UC50 - Generate Monthly Report executed successfully")
                .data(Map.of(
                    "uc", "UC50",
                    "name", "Generate Monthly Report",
                    "description", "Tạo báo cáo tháng trước và gửi email cho các Admin",
                    "status", "DONE"
                ))
                .build();
    }

    /**
     * Chạy TẤT CẢ UC tự động cùng lúc (UC46 → UC47 → UC49 → UC50).
     */
    @PostMapping("/all")
    public ApiResponse<Map<String, String>> triggerAll() {
        scheduledTaskService.autoUpdateSlots();
        scheduledTaskService.autoCancelUnpaidBookings();
        scheduledTaskService.autoUpdateLoyaltyPoints();
        scheduledTaskService.generateMonthlyReport();
        return ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("All System Automatic UCs executed successfully")
                .data(Map.of(
                    "uc46", "Auto Update Slots - DONE",
                    "uc47", "Auto Cancel Unpaid Bookings - DONE",
                    "uc49", "Auto Update Loyalty Points - DONE",
                    "uc50", "Generate Monthly Report - DONE"
                ))
                .build();
    }
}
