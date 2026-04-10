package com.tourbooking.booking.service;

import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.repository.TokenRepository;
import com.tourbooking.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteUnverifiedUsers() {

        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(180);
        List<User> unverifiedUsers = userRepository.findByIsActiveFalseAndCreatedAtBefore(cutoff);
        
        if (!unverifiedUsers.isEmpty()) {
            for (User user : unverifiedUsers) {
                // XoÃƒÆ’Ã‚Â¡ token nÃƒÂ¡Ã‚ÂºÃ‚Â¿u cÃƒÆ’Ã‚Â³
                tokenRepository.deleteByEmail(user.getEmail());
            }
            // XoÃƒÆ’Ã‚Â¡ user
            userRepository.deleteAll(unverifiedUsers);
            log.info("XoÃƒÆ’Ã‚Â¡ {} users chÃƒâ€ Ã‚Â°a xÃƒÆ’Ã‚Â¡c thÃƒÂ¡Ã‚Â»Ã‚Â±c quÃƒÆ’Ã‚Â¡ 180s.", unverifiedUsers.size());
        }
    }
}
