package com.tourbooking.booking.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tourbooking.booking.backend.config.PayOSProperties;
import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** PayOS: tạo link thanh toán VietQR / Napas và xác minh chữ ký (tài liệu payos.vn — kiểm tra dữ liệu với signature). */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSService {

    private final PayOSProperties payOSProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentLink(Long orderCode, int amount, String description) {
        if (!StringUtils.hasText(payOSProperties.getClientId()) || !StringUtils.hasText(payOSProperties.getApiKey())) {
            throw new AppException(ErrorCode.PAYOS_NOT_CONFIGURED);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("returnUrl", payOSProperties.getReturnUrl());
        body.put("cancelUrl", payOSProperties.getCancelUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSProperties.getClientId().trim());
        headers.set("x-api-key", payOSProperties.getApiKey().trim());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    payOSProperties.getBaseUrl() + "/v2/payment-requests",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("PayOS payment-requests HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.PAYOS_LINK_FAILED);
        } catch (RestClientException e) {
            log.warn("PayOS payment-requests network: {}", e.getMessage());
            throw new AppException(ErrorCode.PAYOS_LINK_FAILED);
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            String checkoutUrl = data.path("checkoutUrl").asText();
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                log.warn("PayOS response missing checkoutUrl: {}", response.getBody());
                throw new AppException(ErrorCode.PAYOS_LINK_FAILED);
            }
            return checkoutUrl;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception e) {
            log.warn("PayOS parse error: {}", e.getMessage());
            throw new AppException(ErrorCode.PAYOS_LINK_FAILED);
        }
    }

    /**
     * Xác minh chữ ký trên object {@code data} (webhook hoặc body trả về từ API lấy thông tin link).
     */
    public boolean verifyPayOsDataSignature(JsonNode dataNode, String providedSignature) {
        if (providedSignature == null || providedSignature.isBlank()
                || dataNode == null || dataNode.isNull() || !dataNode.isObject()) {
            return false;
        }
        String checksumKey = payOSProperties.getChecksumKey();
        if (checksumKey == null || checksumKey.isBlank()) {
            return false;
        }
        try {
            String payload = buildSignatureQueryString(dataNode);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String hex = bytesToHex(hash);
            return hex.equalsIgnoreCase(providedSignature.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gọi PayOS GET /v2/payment-requests/{orderCode} — xác minh chữ ký phần {@code data} nếu có {@code signature}.
     */
    public Optional<JsonNode> fetchPaymentRequestByOrderCode(long orderCode) {
        if (payOSProperties.getClientId() == null || payOSProperties.getApiKey() == null) {
            return Optional.empty();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payOSProperties.getClientId());
        headers.set("x-api-key", payOSProperties.getApiKey());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    payOSProperties.getBaseUrl() + "/v2/payment-requests/" + orderCode,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!"00".equals(root.path("code").asText())) {
                return Optional.empty();
            }
            JsonNode data = root.path("data");
            if (!data.isObject()) {
                return Optional.empty();
            }
            String sig = root.path("signature").asText("");
            if (!sig.isBlank() && !verifyPayOsDataSignature(data, sig)) {
                return Optional.empty();
            }
            return Optional.of(data);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    String buildSignatureQueryString(JsonNode data) {
        TreeMap<String, JsonNode> sorted = new TreeMap<>();
        data.fields().forEachRemaining(e -> sorted.put(e.getKey(), e.getValue()));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, JsonNode> e : sorted.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=');
            sb.append(serializeSignatureValue(e.getValue()));
        }
        return sb.toString();
    }

    private String serializeSignatureValue(JsonNode val) {
        if (val == null || val.isNull()) {
            return "";
        }
        if (val.isBoolean()) {
            return val.booleanValue() ? "true" : "false";
        }
        if (val.isIntegralNumber()) {
            return Long.toString(val.longValue());
        }
        if (val.isNumber()) {
            return val.decimalValue().stripTrailingZeros().toPlainString();
        }
        if (val.isTextual()) {
            return val.asText();
        }
        if (val.isArray()) {
            return serializeArrayForSignature((ArrayNode) val);
        }
        if (val.isObject()) {
            try {
                return objectMapper.writeValueAsString(sortKeysObjectNode(val));
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    private String serializeArrayForSignature(ArrayNode arr) {
        try {
            ArrayNode out = objectMapper.createArrayNode();
            for (JsonNode item : arr) {
                if (item != null && item.isObject()) {
                    out.add(sortKeysObjectNode(item));
                } else {
                    out.add(item);
                }
            }
            return objectMapper.writeValueAsString(out);
        } catch (Exception e) {
            return "[]";
        }
    }

    private ObjectNode sortKeysObjectNode(JsonNode obj) {
        TreeMap<String, JsonNode> sorted = new TreeMap<>();
        obj.fields().forEachRemaining(e -> sorted.put(e.getKey(), e.getValue()));
        ObjectNode out = objectMapper.createObjectNode();
        sorted.forEach(out::set);
        return out;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * So khớp số tiền webhook/API với bản ghi payment (VND nguyên).
     */
    public static boolean amountsMatch(BigDecimal paymentAmount, JsonNode payOsData) {
        if (paymentAmount == null || payOsData == null || !payOsData.has("amount")) {
            return true;
        }
        long remote = payOsData.path("amount").asLong(-1);
        if (remote < 0) {
            return true;
        }
        return paymentAmount.compareTo(BigDecimal.valueOf(remote)) == 0;
    }
}
