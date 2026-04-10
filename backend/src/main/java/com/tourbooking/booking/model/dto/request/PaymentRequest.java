package com.tourbooking.booking.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    private Long bookingId;

    // UC16: chÃƒÂ¡Ã‚Â»Ã‚Ân phÃƒâ€ Ã‚Â°Ãƒâ€ Ã‚Â¡ng thÃƒÂ¡Ã‚Â»Ã‚Â©c
    private String paymentMethod;
    // vÃƒÆ’Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥: VNPAY, MOMO, CASH

    // UC18: thanh toÃƒÆ’Ã‚Â¡n tÃƒÂ¡Ã‚Â»Ã‚Â«ng phÃƒÂ¡Ã‚ÂºÃ‚Â§n (optional)
    private BigDecimal amount;

    // optional: mÃƒÆ’Ã‚Â£ giao dÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch (nÃƒÂ¡Ã‚ÂºÃ‚Â¿u mock)
    private String transactionCode;
}
