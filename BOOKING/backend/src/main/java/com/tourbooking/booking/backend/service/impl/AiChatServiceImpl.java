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
        User user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId()).orElse(null);
        }

        // KHÔNG save tin nhắn user ở đây vì frontend đã save qua /api/v1/chat/messages
        // Chỉ gọi Gemini và lưu phản hồi AI

        String reply = callGemini(request.getMessage());

        // Lưu phản hồi AI vào DB
        ChatMessages aiMsg = new ChatMessages();
        aiMsg.setUser(user);
        aiMsg.setSenderType(ChatSenderType.AI.name());
        aiMsg.setMessage(reply);
        if (user == null && request.getGuestId() != null) {
            aiMsg.setGuestId(request.getGuestId());
        }
        chatRepo.save(aiMsg);

        return AiChatResponse.builder().reply(reply).build();
    }

    // =====================================================================
    // Gọi Gemini API - dùng JSON string trực tiếp để tránh lỗi serialize
    // =====================================================================
    private String callGemini(String userMessage) {
        try {
            String tourContext = buildTourContext(userMessage);

            String systemPrompt =
                "Bạn là trợ lý tư vấn tour du lịch chuyên nghiệp của TourBooking - nền tảng đặt tour tại Việt Nam.\\n\\n" +
                "NGUYÊN TẮC:\\n" +
                "- Luôn trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp\\n" +
                "- Chỉ tư vấn về du lịch, tour, địa điểm, thời tiết, ngân sách\\n" +
                "- Trả lời ngắn gọn, có cấu trúc, dùng emoji phù hợp\\n" +
                "- Cuối mỗi câu hỏi thêm để hiểu rõ nhu cầu khách hàng\\n" +
                "- Nếu khách muốn đặt tour, hướng dẫn xem trang /pages/tours.html\\n\\n" +
                "TOUR HIỆN CÓ TRONG HỆ THỐNG:\\n" +
                tourContext.replace("\"", "\\\"").replace("\n", "\\n") + "\\n\\n" +
                "Câu hỏi của khách: " + userMessage.replace("\"", "\\\"");

            // Build JSON string trực tiếp
            String jsonBody = "{"
                + "\"contents\":[{"
                + "\"parts\":[{\"text\":\"" + systemPrompt + "\"}]"
                + "}],"
                + "\"generationConfig\":{"
                + "\"temperature\":0.7,"
                + "\"maxOutputTokens\":512,"
                + "\"topP\":0.9"
                + "}"
                + "}";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode text = root
                        .path("candidates").get(0)
                        .path("content")
                        .path("parts").get(0)
                        .path("text");

                if (!text.isMissingNode()) {
                    return text.asText();
                }
            }

            log.warn("[Gemini] Unexpected response status: {}", response.getStatusCode());
            return fallbackResponse(userMessage);

        } catch (Exception e) {
            log.error("[Gemini] API call failed: {}", e.getMessage());
            return fallbackResponse(userMessage);
        }
    }

    // Lấy danh sách tour từ DB làm context cho Gemini
    private String buildTourContext(String keyword) {
        try {
            List<Tour> tours = tourRepo.searchToursWithFilters(keyword, null, null, null, null);
            if (tours.isEmpty()) {
                tours = tourRepo.findAll().stream().limit(8).collect(Collectors.toList());
            }
            if (tours.isEmpty()) return "Chưa có dữ liệu tour.";

            return tours.stream().limit(8).map(t ->
                t.getTourName()
                + " | Gia: " + (t.getPrice() != null ? String.format("%,.0f", t.getPrice()) + " VND" : "lien he")
                + " | " + (t.getDuration() != null ? t.getDuration() + " ngay" : "")
                + " | " + (t.getStartLocation() != null ? "tu " + t.getStartLocation() : "")
                + " | " + (t.getEndLocation() != null ? "den " + t.getEndLocation() : "")
                + " | rating: " + (t.getRating() != null ? t.getRating() : "N/A")
            ).collect(Collectors.joining("; "));

        } catch (Exception e) {
            log.warn("[Gemini] Cannot load tour context: {}", e.getMessage());
            return "Khong the tai du lieu tour.";
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
