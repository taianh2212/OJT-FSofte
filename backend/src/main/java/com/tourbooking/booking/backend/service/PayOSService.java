package com.tourbooking.booking.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.config.PayOSProperties;
import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayOSService {

    private final PayOSProperties payOSProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentLink(Long orderCode, int amount, String description) {
        if (payOSProperties.getClientId() == null || payOSProperties.getApiKey() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("returnUrl", payOSProperties.getReturnUrl());
        body.put("cancelUrl", payOSProperties.getCancelUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSProperties.getClientId());
        headers.set("x-api-key", payOSProperties.getApiKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                payOSProperties.getBaseUrl() + "/v2/payment-requests",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            String checkoutUrl = data.path("checkoutUrl").asText();
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            return checkoutUrl;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    public boolean verifyWebhookSignature(String rawPayload, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String checksumKey = payOSProperties.getChecksumKey();
        if (checksumKey == null || checksumKey.isBlank()) {
            return false;
        }
        try {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            sha256.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = sha256.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            String hex = bytesToHex(hash);
            String base64 = Base64.getEncoder().encodeToString(hash);
            return signature.equalsIgnoreCase(hex) || signature.equals(base64);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
