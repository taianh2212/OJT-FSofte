package com.tourbooking.booking.backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.backend.model.dto.request.AiChatRequest;
import com.tourbooking.booking.backend.model.dto.response.AiChatResponse;
import com.tourbooking.booking.backend.model.entity.ChatMessages;
import com.tourbooking.booking.backend.model.entity.Tour;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.ChatSenderType;
import com.tourbooking.booking.backend.repository.ChatMessagesRepository;
import com.tourbooking.booking.backend.repository.TourRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.AiChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final TourRepository tourRepo;
    private final ChatMessagesRepository chatRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        // 1. Load tour data within the caller's context (no lazy-loading issues)
        String tourContext = buildTourContext();

        // 2. Call Gemini OUTSIDE of any transaction to prevent rollback-only issues
        String reply = callGemini(request.getMessage(), tourContext);

        // 3. Persist the AI reply in its own transaction
        saveAiMessage(request, reply);

        return AiChatResponse.builder().reply(reply).build();
    }

    @Transactional
    public void saveAiMessage(AiChatRequest request, String reply) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId()).orElse(null);
        }

        ChatMessages aiMsg = new ChatMessages();
        aiMsg.setUser(user);
        aiMsg.setSenderType(ChatSenderType.AI.name());
        aiMsg.setMessage(reply);
        if (user == null && request.getGuestId() != null) {
            aiMsg.setGuestId(request.getGuestId());
        }
        chatRepo.save(aiMsg);
    }

    // =====================================================================
    // Gọi Gemini API - dùng JSON string trực tiếp để tránh lỗi serialize
    // =====================================================================
    private String callGemini(String userMessage, String tourContext) {
        try {
            String promptText =
                "Bạn là trợ lý tư vấn tour du lịch của TourBooking - nền tảng đặt tour trực tuyến tại Việt Nam.\n\n" +
                "QUY TẮC NGHIÊM NGẶT (PHẢI TUÂN THỦ TUYỆT ĐỐI):\n" +
                "1. CHỈ được giới thiệu các tour CÓ TRONG DANH SÁCH TOUR BÊN DƯỚI - không được tự nghĩ ra tour hoặc địa điểm khác.\n" +
                "2. Nếu khách hỏi về địa điểm không có trong danh sách tour, hãy thông báo rằng hiện tại chưa có tour đó và gợi ý tour gần nhất phù hợp.\n" +
                "3. Luôn trả lời bằng tiếng Việt, thân thiện, có cấu trúc rõ ràng, dùng emoji phù hợp.\n" +
                "4. Khi giới thiệu tour, luôn đề cập TÊN TOUR CHÍNH XÁC, GIÁ, SỐ NGÀY từ danh sách.\n" +
                "5. Cuối câu trả lời, hỏi thêm để hiểu nhu cầu khách hàng (ngân sách, số người, thời gian...).\n" +
                "6. Nếu khách muốn đặt tour, hướng dẫn: 'Vui lòng xem và đặt tour tại /pages/tours.html'.\n" +
                "7. Không bịa đặt thông tin giá, ngày hoặc địa điểm không có trong danh sách.\n\n" +
                "=== DANH SÁCH TOUR HIỆN CÓ TRONG HỆ THỐNG ===\n" +
                tourContext + "\n" +
                "=== HẾT DANH SÁCH TOUR ===\n\n" +
                "Câu hỏi của khách: " + userMessage;

            var payload = new java.util.HashMap<String, Object>();
            var contents = new java.util.ArrayList<java.util.HashMap<String, Object>>();
            var content = new java.util.HashMap<String, Object>();
            var parts = new java.util.ArrayList<java.util.HashMap<String, Object>>();
            var part = new java.util.HashMap<String, Object>();
            part.put("text", promptText);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            payload.put("contents", contents);

            String jsonBody = objectMapper.writeValueAsString(payload);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", geminiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            String url = geminiApiUrl; 

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode text = candidates.get(0)
                            .path("content")
                            .path("parts").get(0)
                            .path("text");

                    if (!text.isMissingNode()) {
                        return text.asText();
                    }
                }
            }

            log.warn("[Gemini] Unexpected response: {} - {}", response.getStatusCode(), response.getBody());
            return fallbackResponse(userMessage);

        } catch (Exception e) {
            log.error("[Gemini] API call failed: {}", e.getMessage(), e);
            return fallbackResponse(userMessage);
        }
    }

    // Load tour data inside a separate transaction
    @Transactional(readOnly = true)
    public String loadTourContext() {
        return buildTourContext();
    }

    // Build tour context string from all tours in DB
    private String buildTourContext() {
        try {
            List<Tour> allTours = tourRepo.findAll();
            if (allTours.isEmpty()) {
                return "Hiện tại hệ thống chưa có tour nào được cập nhật. Vui lòng liên hệ nhân viên tư vấn.";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allTours.size(); i++) {
                Tour t = allTours.get(i);
                sb.append(i + 1).append(". ");
                sb.append("**").append(t.getTourName()).append("**");
                sb.append(" | Giá: ").append(t.getPrice() != null ? String.format("%,.0f VNĐ", t.getPrice()) : "Liên hệ");
                sb.append(" | Thời gian: ").append(t.getDuration() != null ? t.getDuration() + " ngày" : "N/A");
                sb.append(" | Khởi hành từ: ").append(t.getStartLocation() != null ? t.getStartLocation() : "N/A");
                sb.append(" | Điểm đến: ").append(t.getEndLocation() != null ? t.getEndLocation() : "N/A");
                if (t.getTransportType() != null) sb.append(" | Phương tiện: ").append(t.getTransportType());
                if (t.getRating() != null && t.getRating() > 0) sb.append(" | Rating: ").append(t.getRating()).append("/5");
                if (t.getDescription() != null) sb.append(" | Mô tả: ").append(t.getDescription());
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.warn("[Gemini] Cannot load tour context: {}", e.getMessage());
            return "Không thể tải dữ liệu tour lúc này. Vui lòng thử lại sau.";
        }
    }

    // Fallback khi Gemini API không phản hồi được
    private String fallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("nhân viên") || lower.contains("hỗ trợ") || lower.contains("gặp người")) {
            return "Tôi sẽ kết nối bạn với nhân viên tư vấn ngay! Bạn hãy nhấn nút **'Talk to staff'** bên dưới nhé.";
        }
        return "😔 Xin lỗi, hệ thống AI đang tạm thời gặp sự cố.\n\n"
             + "Bạn có thể:\n"
             + "• Xem danh sách tour tại /pages/tours.html\n"
             + "• Nhấn **'Talk to staff'** để gặp nhân viên tư vấn\n"
             + "• Thử lại sau vài giây";
    }
}
