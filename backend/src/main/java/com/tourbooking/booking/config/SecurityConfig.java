package com.tourbooking.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tourbooking.booking.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Cho phÃƒÆ’Ã‚Â©p tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ cÃƒÆ’Ã‚Â¡c tÃƒÆ’Ã‚Â i nguyÃƒÆ’Ã‚Âªn tÃƒâ€žÃ‚Â©nh
                .requestMatchers(
                    "/", "/error", "/index.html", "/favicon.ico",
                    "/css/**", "/js/**", "/images/**", "/assets/**",
                    "/pages/**",
                    "/user/**", "/admin/**",
                    "/static/**", "/webjars/**", "/uploads/**"
                ).permitAll()
                // Cho phÃƒÆ’Ã‚Â©p cÃƒÆ’Ã‚Â¡c API Auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Guest UC01-UC06
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/tours/**").permitAll()
                // UC10 newsletter subscribe
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/newsletters").permitAll()
                // UC11 chat support + UC51 AI chat
                .requestMatchers("/api/v1/chat/**", "/api/v1/ai/**").permitAll()
                // Admin chat escalation dashboard
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Read-only categories
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/categories/**").permitAll()
                // Everything else requires login (can be refined later)
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
