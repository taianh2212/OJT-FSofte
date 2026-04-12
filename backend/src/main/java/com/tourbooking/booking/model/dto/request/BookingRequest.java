package com.tourbooking.booking.model.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.tourbooking.booking.model.entity.enums.BookingStatus;

@Data
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "At least 1 person")
    private Integer numberOfPeople;

    // optional (UC15)
    private String voucherCode;
    
    private BigDecimal totalPrice;
    private String discountCode;
    private BookingStatus status;
}
