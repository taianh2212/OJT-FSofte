
package com.tourbooking.booking.security;

import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.info("User found: {}, password hash exists: {}", user.getEmail(), (user.getPasswordHash() != null));

        // Null-safe: nÃƒÂ¡Ã‚ÂºÃ‚Â¿u role lÃƒÆ’Ã‚Â  null thÃƒÆ’Ã‚Â¬ mÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh lÃƒÆ’Ã‚Â  CUSTOMER
        String roleName = (user.getRole() != null) ? user.getRole().name() : "CUSTOMER";

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Boolean.TRUE.equals(user.getIsActive()),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }
}
