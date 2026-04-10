package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.security.JwtService;
package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthSessionNotificationServiceImplTest {

    @Test
    void subscribeRegistersClientAndNotifyPushesLogoutMessage() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository userRepository = mock(UserRepository.class);
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setEmail("test@example.com");
        user.setCurrentSessionId("session-123");

        when(jwtService.parseClaims("jwt-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(claims.get("sessionId", String.class)).thenReturn("session-123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CapturingService service = new CapturingService(jwtService, userRepository);

        SseEmitter emitter = service.subscribe("jwt-token");
        assertEquals(1, service.createdEmitters);

        service.notifySessionInvalidated("test@example.com",
                "Tài khoản đã được đăng nhập ở nơi khác. Vui lòng đăng nhập lại.");

        assertEquals(1, service.lastEmitter.sentMessages.size());
        assertEquals("Tài khoản đã được đăng nhập ở nơi khác. Vui lòng đăng nhập lại.",
                "TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p ÃƒÂ¡Ã‚Â»Ã…Â¸ nÃƒâ€ Ã‚Â¡i khÃƒÆ’Ã‚Â¡c. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.");

        assertEquals(1, service.lastEmitter.sentMessages.size());
        assertEquals("TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p ÃƒÂ¡Ã‚Â»Ã…Â¸ nÃƒâ€ Ã‚Â¡i khÃƒÆ’Ã‚Â¡c. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.",
                service.lastEmitter.sentMessages.get(0));
    }

    @Test
    void subscribeRejectsStaleSession() {
        JwtService jwtService = mock(JwtService.class);
        UserRepository userRepository = mock(UserRepository.class);
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setEmail("test@example.com");
        user.setCurrentSessionId("session-new");

        when(jwtService.parseClaims("jwt-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(claims.get("sessionId", String.class)).thenReturn("session-old");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CapturingService service = new CapturingService(jwtService, userRepository);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.subscribe("jwt-token"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getStatusCode().value());
    }

    private static final class CapturingService extends AuthSessionNotificationServiceImpl {
        private CapturingEmitter lastEmitter;
        private int createdEmitters;

        private CapturingService(JwtService jwtService, UserRepository userRepository) {
            super(jwtService, userRepository);
        }

        @Override
        protected SseEmitter createEmitter() {
            this.lastEmitter = new CapturingEmitter();
            this.createdEmitters++;
            return this.lastEmitter;
        }
    }

    private static final class CapturingEmitter extends SseEmitter {
        private final java.util.List<Object> sentMessages = new java.util.ArrayList<>();

        @Override
        public void send(Object object) throws IOException {
            sentMessages.add(object);
        }
    }
}
