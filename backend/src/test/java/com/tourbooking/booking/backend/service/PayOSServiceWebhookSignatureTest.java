package com.tourbooking.booking.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.config.PayOSProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vector từ <a href="https://payos.vn/docs/tich-hop-webhook/kiem-tra-du-lieu-voi-signature/">PayOS — Kiểm tra dữ liệu với signature</a>
 */
class PayOSServiceWebhookSignatureTest {

    @Test
    void verifiesOfficialWebhookSample() throws Exception {
        String json = """
                {
                  "code": "00",
                  "desc": "success",
                  "success": true,
                  "data": {
                    "orderCode": 123,
                    "amount": 3000,
                    "description": "VQRIO123",
                    "accountNumber": "12345678",
                    "reference": "TF230204212323",
                    "transactionDateTime": "2023-02-04 18:25:00",
                    "currency": "VND",
                    "paymentLinkId": "124c33293c43417ab7879e14c8d9eb18",
                    "code": "00",
                    "desc": "Thành công",
                    "counterAccountBankId": "",
                    "counterAccountBankName": "",
                    "counterAccountName": "",
                    "counterAccountNumber": "",
                    "virtualAccountName": "",
                    "virtualAccountNumber": ""
                  },
                  "signature": "412e915d2871504ed31be63c8f62a149a4410d34c4c42affc9006ef9917eaa03"
                }
                """;
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(json);
        PayOSProperties props = new PayOSProperties();
        props.setChecksumKey("1a54716c8f0efb2744fb28b6e38b25da7f67a925d98bc1c18bd8faaecadd7675");
        PayOSService svc = new PayOSService(props, om);
        assertTrue(svc.verifyPayOsDataSignature(root.get("data"), root.get("signature").asText()));
    }
}
