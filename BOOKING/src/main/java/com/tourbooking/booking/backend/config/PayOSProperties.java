package com.tourbooking.booking.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payos") // giữ nguyên prefix
public class PayOSProperties {

    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String baseUrl = "https://api-merchant.payos.vn";
    private String returnUrl = "http://localhost:8080/pages/user/payment-return.html";
    private String cancelUrl = "http://localhost:8080/pages/user/checkout.html";

    // Getter & Setter đã có nhờ @Getter @Setter
}