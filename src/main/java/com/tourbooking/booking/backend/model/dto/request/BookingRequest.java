package com.tourbooking.booking.backend.model.dto.request;

import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookingRequest {
    private Long userId;
    private Long scheduleId;
    private Integer numberOfPeople;
    private BigDecimal totalPrice;
    private String discountCode;
    private BookingStatus status;
}
