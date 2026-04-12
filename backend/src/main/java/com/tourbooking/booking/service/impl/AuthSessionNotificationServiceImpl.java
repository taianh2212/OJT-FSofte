package com.tourbooking.booking.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.security.JwtService;
import com.tourbooking.booking.service.AuthSessionNotificationService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthSessionNotificationServiceImpl implements AuthSessionNotificationService {

    private static final long EMITTER_TIMEOUT_MS = 30L * 60L * 1000L;

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ClientConnection>> connections = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String token) {
        Claims claims = jwtService.parseClaims(token);
        String email = claims.getSubject();
        String sessionId = claims.get("sessionId", String.class);

        if (email == null || sessionId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "PhiÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p khÃƒÆ’Ã‚Â´ng hÃƒÂ¡Ã‚Â»Ã‚Â£p lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ hoÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n.");
        }

        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getCurrentSessionId() == null || !sessionId.equals(user.getCurrentSessionId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p ÃƒÂ¡Ã‚Â»Ã…Â¸ nÃƒâ€ Ã‚Â¡i khÃƒÆ’Ã‚Â¡c. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.");
        }

        SseEmitter emitter = createEmitter();
        ClientConnection connection = new ClientConnection(sessionId, emitter);
        connections.computeIfAbsent(email, key -> new CopyOnWriteArrayList<>()).add(connection);

        Runnable cleanup = () -> removeConnection(email, connection);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        return emitter;
    }

    @Override
    public void notifySessionInvalidated(String email, String message) {
        List<ClientConnection> currentConnections = connections.remove(email);
        if (currentConnections == null || currentConnections.isEmpty()) {
            return;
        }

        for (ClientConnection connection : currentConnections) {
            try {
                connection.emitter().send(Objects.requireNonNullElse(message, "TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p ÃƒÂ¡Ã‚Â»Ã…Â¸ nÃƒâ€ Ã‚Â¡i khÃƒÆ’Ã‚Â¡c. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i."));
            } catch (IOException ignored) {
                // If the client is already gone, the cleanup below will discard it.
            } finally {
                connection.emitter().complete();
            }
        }
    }

    protected SseEmitter createEmitter() {
        return new SseEmitter(EMITTER_TIMEOUT_MS);
    }

    private void removeConnection(String email, ClientConnection connection) {
        CopyOnWriteArrayList<ClientConnection> current = connections.get(email);
        if (current == null) {
            return;
        }
        current.remove(connection);
        if (current.isEmpty()) {
            connections.remove(email, current);
        }
    }

    private record ClientConnection(String sessionId, SseEmitter emitter) {
    }
}
