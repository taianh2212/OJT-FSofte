package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.exception.AppException;
import com.tourbooking.booking.exception.ErrorCode;
import com.tourbooking.booking.model.entity.Document;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.repository.DocumentRepository;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public Document upload(Long userId, String url, String type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Document doc = new Document();

        doc.setUser(user);
        doc.setFileUrl(url);
        doc.setType(type);
        doc.setUploadedAt(LocalDateTime.now());

        return documentRepository.save(doc);
    }
}
