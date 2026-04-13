package com.tourbooking.booking.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.tourbooking.booking.backend.repository.UserRepository;
import org.springframework.util.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
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
                // Chỉ set Authentication nếu user tồn tại và sessionId khớp
                if (user != null && user.getCurrentSessionId() != null && sessionId.equals(user.getCurrentSessionId())) {
                    String roleName = (user.getRole() != null) ? user.getRole().name() : "CUSTOMER";
                    boolean enabled = Boolean.TRUE.equals(user.getIsActive());
                    if (!enabled) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    var userDetails = new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            user.getPasswordHash() != null ? user.getPasswordHash() : "",
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
                    );
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            // Token không hợp lệ hoặc hết hạn -> Không set Authentication, 
            // các endpoint yêu cầu login sẽ tự bị chặn ở SecurityFilterChain
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
