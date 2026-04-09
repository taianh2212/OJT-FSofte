<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/dto/response/BookingResponse.java
package com.tourbooking.booking.model.dto.response;

import com.tourbooking.booking.model.entity.enums.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long scheduleId;
    private String tourName;
    private LocalDateTime bookingDate;
    private Integer numberOfPeople;
    private BigDecimal totalPrice;
    private BookingStatus status;
}
========
package com.tourbooking.booking.backend.model.dto.response;

import com.tourbooking.booking.backend.model.entity.enums.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long scheduleId;
    private String tourName;
    private LocalDateTime bookingDate;
    private Integer numberOfPeople;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private String discountCode;
    private BookingStatus status;
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/dto/response/BookingResponse.java
