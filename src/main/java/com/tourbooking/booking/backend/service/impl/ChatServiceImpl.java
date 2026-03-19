package com.tourbooking.booking.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourbooking.booking.backend.model.dto.request.ChatMessageRequest;
import com.tourbooking.booking.backend.model.dto.response.ChatMessageResponse;
import com.tourbooking.booking.backend.model.entity.ChatMessages;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.ChatSenderType;
import com.tourbooking.booking.backend.repository.ChatMessagesRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.ChatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessagesRepository chatRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        ChatMessages entity = new ChatMessages();

        String sender = request.getSenderType() == null ? null : request.getSenderType().trim().toUpperCase();
        if (sender == null || sender.isBlank()) {
            sender = ChatSenderType.GUEST.name();
        }
        try {
            ChatSenderType.valueOf(sender);
        } catch (Exception e) {
            sender = ChatSenderType.GUEST.name();
        }

        if (request.getUserId() != null) {
            User user = userRepo.findById(request.getUserId()).orElse(null);
            entity.setUser(user);
        }

        entity.setSenderType(sender);
        entity.setMessage(request.getMessage());

        ChatMessages saved = chatRepo.save(entity);
        return toResponse(saved);
    }

    @Override
    public List<ChatMessageResponse> getConversation(Long userId) {
        List<ChatMessages> messages = (userId == null)
                ? chatRepo.findByUserIsNullOrderBySentAtAsc()
                : chatRepo.findByUser_IdOrderBySentAtAsc(userId);

        return messages.stream().map(ChatServiceImpl::toResponse).toList();
    }

    private static ChatMessageResponse toResponse(ChatMessages m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .userId(m.getUser() == null ? null : m.getUser().getId())
                .senderType(m.getSenderType())
                .message(m.getMessage())
                .sentAt(m.getSentAt())
                .build();
    }
}

