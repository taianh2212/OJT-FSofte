package com.tourbooking.booking.backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final TourRepository tourRepo;
    private final ChatMessagesRepository chatRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId()).orElse(null);
        }

        // Save guest/user message
        ChatMessages userMsg = new ChatMessages();
        userMsg.setUser(user);
        userMsg.setSenderType(ChatSenderType.GUEST.name());
        userMsg.setMessage(request.getMessage());
        chatRepo.save(userMsg);

        String reply = generateSuggestion(request.getMessage());

        // Save AI reply
        ChatMessages aiMsg = new ChatMessages();
        aiMsg.setUser(user);
        aiMsg.setSenderType(ChatSenderType.AI.name());
        aiMsg.setMessage(reply);
        chatRepo.save(aiMsg);

        return AiChatResponse.builder().reply(reply).build();
    }

    private String generateSuggestion(String message) {
        String keyword = message == null ? "" : message.trim();
        if (keyword.isBlank()) {
            return "Bạn muốn đi đâu (VD: Đà Nẵng, Phú Quốc) và ngân sách khoảng bao nhiêu? Mình sẽ gợi ý tour phù hợp.";
        }

        List<Tour> matches = tourRepo.findByTourNameContainingIgnoreCase(keyword);
        if (matches.isEmpty()) {
            return "Mình chưa thấy tour khớp với \"" + keyword + "\". Bạn thử nhập tên địa điểm hoặc loại tour (biển/núi/văn hoá) nhé.";
        }

        List<Tour> top = matches.stream()
                .sorted((a, b) -> {
                    double ra = a.getRating() == null ? 0 : a.getRating();
                    double rb = b.getRating() == null ? 0 : b.getRating();
                    return Double.compare(rb, ra);
                })
                .limit(5)
                .toList();

        String list = top.stream()
                .map(t -> "- " + t.getTourName() + " (Giá: " + t.getPrice() + ", Rating: " + t.getRating() + ")")
                .collect(Collectors.joining("\n"));

        return "Mình gợi ý một vài tour phù hợp:\n" + list + "\n\nBạn muốn mình lọc theo ngày khởi hành hoặc khoảng giá không?";
    }
}

