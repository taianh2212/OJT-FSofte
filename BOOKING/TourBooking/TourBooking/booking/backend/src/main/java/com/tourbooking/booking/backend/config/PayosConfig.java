package com.tourbooking.booking.backend.config;

import vn.payos.PayOS;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PayosConfig {

    private final PayOSProperties payOSProperties;

    @Bean
    public PayOS payOS() {
        return new PayOS(
            payOSProperties.getClientId().trim(),
            payOSProperties.getApiKey().trim(),
            payOSProperties.getChecksumKey().trim()
        );
    }
}
