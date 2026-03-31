package com.tourbooking.booking.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.UserMapper;
import com.tourbooking.booking.backend.model.dto.request.UserRequest;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.model.entity.Token;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.tourbooking.booking.backend.repository.TokenRepository tokenRepository;

    public UserServiceImpl(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder, 
                           com.tourbooking.booking.backend.repository.TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = UserMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false); // Require verification

        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserMapper.updateEntityFromRequest(user, request);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void saveVerificationToken(String email, String tokenValue) {
        Token token = new Token();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        token.setType("VERIFY");
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String tokenValue) {
        Token token = tokenRepository.findByTokenAndType(tokenValue, "VERIFY")
                .orElse(null);

        if (token == null || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setIsActive(true);
        userRepository.save(user);
        tokenRepository.delete(token);

        return true;
    }

    @Override
    @Transactional
    public void saveResetPasswordToken(String email, String tokenValue) {
        Token token = new Token();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setType("RESET");
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public boolean resetPassword(String tokenValue, String newPassword) {
        Token token = tokenRepository.findByTokenAndType(tokenValue, "RESET")
                .orElse(null);

        if (token == null || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(token);

        return true;
    }

    @Override
    @Transactional
    public String rotateSession(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newSessionId = UUID.randomUUID().toString();
        user.setCurrentSessionId(newSessionId);
        userRepository.save(user);

        return newSessionId;
    }

    @Override
    @Transactional
    public void clearSession(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setCurrentSessionId(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(isActive);
        userRepository.save(user);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public long countOnlineUsers() {
        return userRepository.countByCurrentSessionIdIsNotNull();
    }
}
