package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.request.AuthRequest;
import com.tourbooking.booking.backend.model.dto.request.UserRequest;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.AuthResponse;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserResponse user = userService.getUserByEmail(request.getEmail());
        AuthResponse authResponse = AuthResponse.builder()
                .token("mock-jwt-token") // We will implement real JWT later
                .user(user)
                .build();

        return ApiResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .data(authResponse)
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody UserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(userService.createUser(request))
                .build();
    }
}
