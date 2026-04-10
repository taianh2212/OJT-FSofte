package com.tourbooking.booking.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AuthSessionNotificationService {
    SseEmitter subscribe(String token);

    void notifySessionInvalidated(String email, String message);
}
