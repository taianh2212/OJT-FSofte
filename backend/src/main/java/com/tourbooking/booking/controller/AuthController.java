package com.tourbooking.booking.controller;

import com.tourbooking.booking.model.dto.request.AuthRequest;
import com.tourbooking.booking.model.dto.request.UserRequest;
import com.tourbooking.booking.model.dto.response.ApiResponse;
import com.tourbooking.booking.model.dto.response.AuthResponse;
import com.tourbooking.booking.model.dto.response.UserResponse;
import com.tourbooking.booking.security.JwtService;
import com.tourbooking.booking.service.AuthSessionNotificationService;
import com.tourbooking.booking.service.MailService;
import com.tourbooking.booking.service.UserService;
import com.tourbooking.booking.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final MailService mailService;
    private final RateLimiterService rateLimiterService;
    private final AuthSessionNotificationService authSessionNotificationService;
    private final JwtService jwtService;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;



    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserResponse user = userService.getUserByEmail(request.getEmail());

        if (!user.getIsActive()) {
            throw new RuntimeException("TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n cÃƒÂ¡Ã‚Â»Ã‚Â§a bÃƒÂ¡Ã‚ÂºÃ‚Â¡n chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c xÃƒÆ’Ã‚Â¡c thÃƒÂ¡Ã‚Â»Ã‚Â±c hoÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ khÃƒÆ’Ã‚Â³a. Vui lÃƒÆ’Ã‚Â²ng liÃƒÆ’Ã‚Âªn hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ Admin.");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please check your email.");
        }

        String sessionId = userService.rotateSession(user.getEmail());

        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtService.generateToken(user, sessionId))
                .user(user)
                .build();

        authSessionNotificationService.notifySessionInvalidated(
                user.getEmail(),
                "TÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p ÃƒÂ¡Ã‚Â»Ã…Â¸ nÃƒâ€ Ã‚Â¡i khÃƒÆ’Ã‚Â¡c. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.");

        return ApiResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .data(authResponse)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ApiResponse.<UserResponse>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .data(null)
                    .build();
        }

        UserResponse user = userService.getUserByEmail(principal.getName());
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Current user")
                .data(user)
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody UserRequest request) {
        if (!rateLimiterService.tryConsume(request.getEmail())) {
            return ApiResponse.<UserResponse>builder()
                    .code(HttpStatus.TOO_MANY_REQUESTS.value())
                    .message("Too many registration requests. Please try again later.")
                    .data(null)
                    .build();
        }

        UserResponse user = userService.createUser(request);

        try {
            String token = UUID.randomUUID().toString();

            // lÃƒâ€ Ã‚Â°u token vÃƒÆ’Ã‚Â o DB (bÃƒÂ¡Ã‚ÂºÃ‚Â¡n cÃƒÂ¡Ã‚ÂºÃ‚Â§n tÃƒÂ¡Ã‚Â»Ã‚Â± implement)
            userService.saveVerificationToken(user.getEmail(), token);

            mailService.sendVerificationEmail(user.getEmail(), token);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Register successful. Please check your email to verify.")
                .data(user)
                .build();
    }

    @GetMapping("/verify")
    public ApiResponse<String> verifyEmail(@RequestParam String token) {
        boolean isValid = userService.verifyEmail(token);

        if (!isValid) {
            return ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid or expired token")
                    .data(null)
                    .build();
        }

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Email verified successfully")
                .data(null)
                .build();
    }


    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody AuthRequest request) {
        try {
            String token = UUID.randomUUID().toString();
            userService.saveResetPasswordToken(request.getEmail(), token);
            String baseUrl = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl;
            String resetLink = baseUrl + "/pages/auth/reset-password.html?token=" + token;
            mailService.sendPasswordResetEmail(request.getEmail(), resetLink);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("If email exists, reset link has been sent")
                .data(null)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        boolean success = userService.resetPassword(token, newPassword);

        if (!success) {
            return ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid or expired token")
                    .data(null)
                    .build();
        }

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Password reset successful")
                .data(null)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(Principal principal) {
        if (principal != null && principal.getName() != null) {
            userService.clearSession(principal.getName());
            authSessionNotificationService.notifySessionInvalidated(
                    principal.getName(),
                    "PhiÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.");
        }

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Logout successful")
                .data(null)
                .build();
    }
}
