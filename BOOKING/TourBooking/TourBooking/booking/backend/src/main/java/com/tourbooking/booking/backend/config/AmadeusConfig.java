package com.tourbooking.booking.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
@Slf4j
public class AmadeusConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(15))
                .additionalInterceptors(new AmadeusLoggingInterceptor())
                .build();
    }

    private static class AmadeusLoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        @org.springframework.lang.NonNull
        public ClientHttpResponse intercept(@org.springframework.lang.NonNull HttpRequest request, @org.springframework.lang.NonNull byte[] body, @org.springframework.lang.NonNull ClientHttpRequestExecution execution) throws IOException {
            log.debug("[Amadeus] Request {} {}", request.getMethod(), request.getURI());
            log.debug("[Amadeus] Headers: {}", maskAuthorization(request.getHeaders()));
            ClientHttpResponse response = execution.execute(request, body);
            log.debug("[Amadeus] Response status: {}", response.getStatusCode());
            return response;
        }

        private String maskAuthorization(HttpHeaders headers) {
            String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
            return auth != null ? auth.replaceAll("Bearer\\s+.*", "Bearer ******") : "n/a";
        }
    }
}
