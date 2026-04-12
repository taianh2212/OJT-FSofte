package com.tourbooking.booking.controller;

import com.tourbooking.booking.model.dto.response.ApiResponse;
import com.tourbooking.booking.model.entity.Document;
import com.tourbooking.booking.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload-document")
    public ApiResponse<Document> uploadDoc(@RequestParam Long userId, @RequestParam String url,
            @RequestParam String type) {
        return ApiResponse.success(((DocumentService) documentService).upload(userId, url, type));
    }
}
