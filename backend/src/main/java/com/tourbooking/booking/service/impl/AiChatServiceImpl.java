package com.tourbooking.booking.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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

    @Override
    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId()).orElse(null);
        }

        // KHÃƒÆ’Ã¢â‚¬ÂNG save tin nhÃƒÂ¡Ã‚ÂºÃ‚Â¯n user ÃƒÂ¡Ã‚Â»Ã…Â¸ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¢y vÃƒÆ’Ã‚Â¬ frontend Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ save qua /api/v1/chat/messages
        // ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° gÃƒÂ¡Ã‚Â»Ã‚Âi Gemini vÃƒÆ’Ã‚Â  lÃƒâ€ Ã‚Â°u phÃƒÂ¡Ã‚ÂºÃ‚Â£n hÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“i AI

        String reply = callGemini(request.getMessage());

        // LÃƒâ€ Ã‚Â°u phÃƒÂ¡Ã‚ÂºÃ‚Â£n hÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“i AI vÃƒÆ’Ã‚Â o DB
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

    // GÃƒÂ¡Ã‚Â»Ã‚Âi Gemini API - dÃƒÆ’Ã‚Â¹ng JSON string trÃƒÂ¡Ã‚Â»Ã‚Â±c tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ trÃƒÆ’Ã‚Â¡nh lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i serialize
    private String callGemini(String userMessage) {
        try {
            String tourContext = buildTourContext(userMessage);

            String systemPrompt =
                "BÃƒÂ¡Ã‚ÂºÃ‚Â¡n lÃƒÆ’Ã‚Â  trÃƒÂ¡Ã‚Â»Ã‚Â£ lÃƒÆ’Ã‚Â½ tÃƒâ€ Ã‚Â° vÃƒÂ¡Ã‚ÂºÃ‚Â¥n tour du lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch chuyÃƒÆ’Ã‚Âªn nghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p cÃƒÂ¡Ã‚Â»Ã‚Â§a TourBooking - nÃƒÂ¡Ã‚Â»Ã‚Ân tÃƒÂ¡Ã‚ÂºÃ‚Â£ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t tour tÃƒÂ¡Ã‚ÂºÃ‚Â¡i ViÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t Nam.\\n\\n" +
                "NGUYÃƒÆ’Ã…Â N TÃƒÂ¡Ã‚ÂºÃ‚Â®C:\\n" +
                "- LuÃƒÆ’Ã‚Â´n trÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi bÃƒÂ¡Ã‚ÂºÃ‚Â±ng tiÃƒÂ¡Ã‚ÂºÃ‚Â¿ng ViÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t, thÃƒÆ’Ã‚Â¢n thiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n vÃƒÆ’Ã‚Â  chuyÃƒÆ’Ã‚Âªn nghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p\\n" +
                "- ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° tÃƒâ€ Ã‚Â° vÃƒÂ¡Ã‚ÂºÃ‚Â¥n vÃƒÂ¡Ã‚Â»Ã‚Â du lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch, tour, Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹a Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã†â€™m, thÃƒÂ¡Ã‚Â»Ã‚Âi tiÃƒÂ¡Ã‚ÂºÃ‚Â¿t, ngÃƒÆ’Ã‚Â¢n sÃƒÆ’Ã‚Â¡ch\\n" +
                "- TrÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi ngÃƒÂ¡Ã‚ÂºÃ‚Â¯n gÃƒÂ¡Ã‚Â»Ã‚Ân, cÃƒÆ’Ã‚Â³ cÃƒÂ¡Ã‚ÂºÃ‚Â¥u trÃƒÆ’Ã‚Âºc, dÃƒÆ’Ã‚Â¹ng emoji phÃƒÆ’Ã‚Â¹ hÃƒÂ¡Ã‚Â»Ã‚Â£p\\n" +
                "- CuÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi mÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i cÃƒÆ’Ã‚Â¢u hÃƒÂ¡Ã‚Â»Ã‚Âi thÃƒÆ’Ã‚Âªm Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ hiÃƒÂ¡Ã‚Â»Ã†â€™u rÃƒÆ’Ã‚Âµ nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u khÃƒÆ’Ã‚Â¡ch hÃƒÆ’Ã‚Â ng\\n" +
                "- NÃƒÂ¡Ã‚ÂºÃ‚Â¿u khÃƒÆ’Ã‚Â¡ch muÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœn Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t tour, hÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºng dÃƒÂ¡Ã‚ÂºÃ‚Â«n xem trang /pages/tours.html\\n\\n" +
                "TOUR HIÃƒÂ¡Ã‚Â»Ã¢â‚¬Â N CÃƒÆ’Ã¢â‚¬Å“ TRONG HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â  THÃƒÂ¡Ã‚Â»Ã‚ÂNG:\\n" +
                tourContext.replace("\"", "\\\"").replace("\n", "\\n") + "\\n\\n" +
                "CÃƒÆ’Ã‚Â¢u hÃƒÂ¡Ã‚Â»Ã‚Âi cÃƒÂ¡Ã‚Â»Ã‚Â§a khÃƒÆ’Ã‚Â¡ch: " + userMessage.replace("\"", "\\\"");

            // Build JSON string trÃƒÂ¡Ã‚Â»Ã‚Â±c tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p
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

    // LÃƒÂ¡Ã‚ÂºÃ‚Â¥y danh sÃƒÆ’Ã‚Â¡ch tour tÃƒÂ¡Ã‚Â»Ã‚Â« DB lÃƒÆ’Ã‚Â m context cho Gemini
    private String buildTourContext(String keyword) {
        try {
            List<Tour> tours = tourRepo.searchToursWithFilters(keyword, null, null, null, null);
            if (tours.isEmpty()) {
                tours = tourRepo.findAll().stream().limit(8).collect(Collectors.toList());
            }
            if (tours.isEmpty()) return "ChÃƒâ€ Ã‚Â°a cÃƒÆ’Ã‚Â³ dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u tour.";

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

    // Fallback khi Gemini API khÃƒÆ’Ã‚Â´ng phÃƒÂ¡Ã‚ÂºÃ‚Â£n hÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“i Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c
    private String fallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("nhÃƒÆ’Ã‚Â¢n viÃƒÆ’Ã‚Âªn") || lower.contains("hÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ trÃƒÂ¡Ã‚Â»Ã‚Â£") || lower.contains("gÃƒÂ¡Ã‚ÂºÃ‚Â·p ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi")) {
            return "TÃƒÆ’Ã‚Â´i sÃƒÂ¡Ã‚ÂºÃ‚Â½ kÃƒÂ¡Ã‚ÂºÃ‚Â¿t nÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi bÃƒÂ¡Ã‚ÂºÃ‚Â¡n vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi nhÃƒÆ’Ã‚Â¢n viÃƒÆ’Ã‚Âªn tÃƒâ€ Ã‚Â° vÃƒÂ¡Ã‚ÂºÃ‚Â¥n ngay! BÃƒÂ¡Ã‚ÂºÃ‚Â¡n hÃƒÆ’Ã‚Â£y nhÃƒÂ¡Ã‚ÂºÃ‚Â¥n nÃƒÆ’Ã‚Âºt **'Talk to staff'** bÃƒÆ’Ã‚Âªn dÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi nhÃƒÆ’Ã‚Â©.";
        }
        return "ÃƒÂ°Ã…Â¸Ã‹Å“Ã¢â‚¬Â Xin lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i, hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng AI Ãƒâ€žÃ¢â‚¬Ëœang tÃƒÂ¡Ã‚ÂºÃ‚Â¡m thÃƒÂ¡Ã‚Â»Ã‚Âi gÃƒÂ¡Ã‚ÂºÃ‚Â·p sÃƒÂ¡Ã‚Â»Ã‚Â± cÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ.\n\n"
             + "BÃƒÂ¡Ã‚ÂºÃ‚Â¡n cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™:\n"
             + "ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ Xem danh sÃƒÆ’Ã‚Â¡ch tour tÃƒÂ¡Ã‚ÂºÃ‚Â¡i /pages/tours.html\n"
             + "ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ NhÃƒÂ¡Ã‚ÂºÃ‚Â¥n **'Talk to staff'** Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ gÃƒÂ¡Ã‚ÂºÃ‚Â·p nhÃƒÆ’Ã‚Â¢n viÃƒÆ’Ã‚Âªn tÃƒâ€ Ã‚Â° vÃƒÂ¡Ã‚ÂºÃ‚Â¥n\n"
             + "ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ ThÃƒÂ¡Ã‚Â»Ã‚Â­ lÃƒÂ¡Ã‚ÂºÃ‚Â¡i sau vÃƒÆ’Ã‚Â i giÃƒÆ’Ã‚Â¢y";
    }
}
