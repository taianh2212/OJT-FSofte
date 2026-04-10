package com.tourbooking.booking.service;

import com.tourbooking.booking.model.entity.Document;

public interface DocumentService {
    Document upload(Long userId, String url, String type);
}
