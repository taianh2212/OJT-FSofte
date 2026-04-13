package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.service.AuthSessionNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSessionNotificationController {

    private final AuthSessionNotificationService authSessionNotificationService;

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@RequestParam String token) {
        return authSessionNotificationService.subscribe(token);
    }
}
