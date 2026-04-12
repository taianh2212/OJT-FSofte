package com.tourbooking.booking.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.tourbooking.booking.repository.UserRepository;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtService.parseClaims(token);
            String email = claims.getSubject();
            String sessionId = claims.get("sessionId", String.class);

            if (email != null && sessionId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var user = userRepository.findByEmail(email).orElse(null);
                // ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° set Authentication nÃƒÂ¡Ã‚ÂºÃ‚Â¿u user tÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i vÃƒÆ’Ã‚Â  sessionId khÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºp
                if (user != null && user.getCurrentSessionId() != null && sessionId.equals(user.getCurrentSessionId())) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authenticated user: {} with roles: {}", email, userDetails.getAuthorities());
                } else {
                    log.warn("Session mismatch or user not found for email: {}. DB Session: {}, JWT Session: {}", 
                        email, user != null ? user.getCurrentSessionId() : "N/A", sessionId);
                }
            }
        } catch (Exception ignored) {
            // Token khÃƒÆ’Ã‚Â´ng hÃƒÂ¡Ã‚Â»Ã‚Â£p lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ hoÃƒÂ¡Ã‚ÂºÃ‚Â·c hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n -> KhÃƒÆ’Ã‚Â´ng set Authentication, 
            // cÃƒÆ’Ã‚Â¡c endpoint yÃƒÆ’Ã‚Âªu cÃƒÂ¡Ã‚ÂºÃ‚Â§u login sÃƒÂ¡Ã‚ÂºÃ‚Â½ tÃƒÂ¡Ã‚Â»Ã‚Â± bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ chÃƒÂ¡Ã‚ÂºÃ‚Â·n ÃƒÂ¡Ã‚Â»Ã…Â¸ SecurityFilterChain
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring("Bearer ".length()).trim();
        }
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        return null;
    }
}
