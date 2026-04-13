package com.tourbooking.booking.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbooking.booking.model.dto.request.AiChatRequest;
import com.tourbooking.booking.model.dto.response.AiChatResponse;
import com.tourbooking.booking.model.entity.ChatMessages;
import com.tourbooking.booking.model.entity.Tour;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.model.entity.enums.ChatSenderType;
import com.tourbooking.booking.repository.ChatMessagesRepository;
import com.tourbooking.booking.repository.TourRepository;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.service.AiChatService;

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

    @Value("${app.public-base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId()).orElse(null);
        }

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

    private String callGemini(String userMessage) {
        try {
            // Trích xuất từ khóa thô (loại bỏ từ thừa) để search DB tốt hơn
            String searchKeyword = extractKeyword(userMessage);
            String tourContext = buildTourContext(searchKeyword);

            String systemPrompt =
                "Bạn là chuyên viên tư vấn du lịch của TourBooking.\\n\\n" +
                "NGUYÊN TẮC QUAN TRỌNG:\\n" +
                "1. Luôn trả lời bằng tiếng Việt, thân thiện, ngắn gọn.\\n" +
                "2. CHỈ giới thiệu các tour CÓ TRONG DANH SÁCH BÊN DƯỚI. TUYỆT ĐỐI KHÔNG TỰ BỊA RA TOUR KHÔNG CÓ TRONG DANH SÁCH.\\n" +
                "3. Khi khách muốn đi một địa điểm (ví dụ: Đà Nẵng), HÃY TÌM và LIỆT KÊ TRỰC TIẾP tối đa 5 tour ở địa điểm đó.\\n" +
                "4. CHẮC CHẮN PHẢI CUNG CẤP GIÁ TIỀN và ĐƯỜNG LINK cho mỗi tour. Link lấy chính xác từ trường 'Link đặt tour'.\\n" +
                "5. KHÔNG hỏi thêm các câu thừa thãi nếu đã có địa điểm.\\n" +
                "6. Nếu không có tour nào khớp (hoặc khi khách hỏi chung chung), hãy giới thiệu TỐI ĐA 5 tour nổi bật nhất trong danh sách.\\n\\n" +
                
                "ĐỊNH DẠNG MỖI TOUR KHUYÊN DÙNG:\\n" +
                "🌟 **[Tên tour]**\\n" +
                "💰 Giá: [Giá tiền] | ⏱ [Thời gian]\\n" +
                "📍 [Điểm đi] → [Điểm đến]\\n" +
                "🔗 [xem thêm tại đây](LINK_ĐẶT_TOUR)\\n\\n" +
                
                "DANH SÁCH TOUR TRONG HỆ THỐNG:\\n" +
                tourContext.replace("\"", "\\\"").replace("\n", "\\n") + "\\n\\n" +
                "Yêu cầu của khách: " + userMessage.replace("\"", "\\\"").replace("\n", "\\n");

            String jsonBody = "{"
                + "\"contents\":[{"
                + "\"parts\":[{\"text\":\"" + systemPrompt + "\"}]"
                + "}],"
                + "\"generationConfig\":{"
                + "\"temperature\":0.5,"
                + "\"maxOutputTokens\":2048,"
                + "\"topP\":0.9"
                + "}"
                + "}";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", geminiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, entity, String.class);

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

    /**
     * Xây dựng context tour chi tiết từ DB để truyền cho Gemini.
     * Bao gồm: ID (cho link), tên, giá, thời gian, điểm đi/đến, rating, mô tả, có pickup/lunch không.
     */
    private String buildTourContext(String keyword) {
        try {
            List<Tour> tours;
            if (keyword != null && !keyword.isBlank()) {
                // Liệt kê các tour khớp từ khóa
                tours = tourRepo.searchToursWithFilters(keyword, null, null, null, null);
                
                // Nếu search theo cả cụm từ không ra, thử search theo từng từ đơn (nếu từ > 2 ký tự)
                if (tours.isEmpty()) {
                    String[] words = keyword.split("\\s+");
                    for (String word : words) {
                        if (word.length() > 2) {
                            List<Tour> wordMatch = tourRepo.searchToursWithFilters(word, null, null, null, null);
                            if (!wordMatch.isEmpty()) {
                                tours = wordMatch;
                                break;
                            }
                        }
                    }
                }
            } else {
                tours = java.util.Collections.emptyList();
            }

            // Nếu vẫn không thấy hoặc keyword rỗng, lấy top 20 tour mới nhất/nổi bật
            if (tours.isEmpty()) {
                tours = tourRepo.findAllWithBasicDetails().stream().limit(20).collect(Collectors.toList());
            }

            if (tours.isEmpty()) return "Hiện chưa có tour nào trong hệ thống.";

            return tours.stream().limit(20).map(t -> {
                String tourId = t.getId() != null ? String.valueOf(t.getId()) : "?";
                String name = t.getTourName() != null ? t.getTourName() : "Không tên";
                String price = t.getPrice() != null
                    ? String.format("%,.0f VND", t.getPrice())
                    : "Liên hệ";
                String duration = t.getDuration() != null ? t.getDuration() + " ngày" : "N/A";
                String from = t.getStartLocation() != null ? t.getStartLocation() : "N/A";
                String to = t.getEndLocation() != null ? t.getEndLocation() : "N/A";
                String rating = t.getRating() != null ? String.format("%.1f/5", t.getRating()) : "Chưa có";
                String transport = t.getTransportType() != null ? t.getTransportType() : "N/A";
                boolean hasPickup = Boolean.TRUE.equals(t.getHasPickup());
                boolean hasLunch = Boolean.TRUE.equals(t.getHasLunch());

                // Cắt mô tả nếu quá dài
                String desc = t.getDescription() != null
                    ? (t.getDescription().length() > 200
                        ? t.getDescription().substring(0, 200) + "..."
                        : t.getDescription())
                    : "";

                String link = baseUrl + "/pages/tour-detail.html?id=" + tourId;

                return "---" +
                    "\nID: " + tourId +
                    "\nTên tour: " + name +
                    "\nGiá: " + price +
                    "\nThời gian: " + duration +
                    "\nKhởi hành: " + from + " → " + to +
                    "\nPhương tiện: " + transport +
                    "\nRating: " + rating +
                    "\nCó đón/trả: " + (hasPickup ? "Có" : "Không") +
                    "\nCó bữa trưa: " + (hasLunch ? "Có" : "Không") +
                    "\nMô tả: " + desc +
                    "\nLink đặt tour: " + link;

            }).collect(Collectors.joining("\n"));

        } catch (Exception e) {
            log.warn("[Gemini] Cannot load tour context: {}", e.getMessage());
            return "Không thể tải dữ liệu tour lúc này.";
        }
    }

    /**
     * Loại bỏ các từ thừa/ngữ cảnh để lấy từ khóa chính (Địa danh, tên tour)
     */
    private String extractKeyword(String message) {
        if (message == null) return "";
        return message.toLowerCase()
            .replace("tôi muốn đi", "")
            .replace("tìm tour", "")
            .replace("giới thiệu", "")
            .replace("có những sản phẩm nào", "")
            .replace("sản phẩm", "")
            .replace("tour du lịch", "")
            .replace("tư vấn", "")
            .replace("đến", "")
            .replace("tại", "")
            .replace("đi", "")
            .trim();
    }

    private String fallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("nhân viên") || lower.contains("hỗ trợ") || lower.contains("gặp người")) {
            return "Tôi sẽ kết nối bạn với nhân viên tư vấn ngay! Bạn hãy nhấn nút **'Talk to staff'** bên dưới nhé.";
        }
        return "🤖 Xin lỗi, hệ thống AI đang tạm thời gặp sự cố.\n\n"
             + "Bạn có thể:\n"
             + "• Xem danh sách tour tại " + baseUrl + "/pages/tours.html\n"
             + "• Nhấn **'Talk to staff'** để gặp nhân viên tư vấn\n"
             + "• Thử lại sau vài giây";
    }
}
