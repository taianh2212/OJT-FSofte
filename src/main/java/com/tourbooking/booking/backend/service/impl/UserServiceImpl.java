package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.exception.AppException;
import com.tourbooking.booking.backend.exception.ErrorCode;
import com.tourbooking.booking.backend.mapper.UserMapper;
import com.tourbooking.booking.backend.model.dto.request.UserRequest;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourbooking.booking.backend.model.entity.Token;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.tourbooking.booking.backend.repository.TokenRepository tokenRepository;

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
        
        // Gán Role mặc định là CUSTOMER khi đăng ký
        if (user.getRole() == null) {
            user.setRole(com.tourbooking.booking.backend.model.entity.enums.UserRole.CUSTOMER);
        }
        
        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
        }

        UserMapper.updateEntityFromRequest(existingUser, request);
        if (request.getPassword() != null) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
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
    public void saveVerificationToken(String email, String token) {
        Token t = Token.builder()
                .token(token)
                .email(email)
                .type("VERIFY")
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(t);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        Token t = tokenRepository.findByToken(token)
                .orElse(null);

        if (t == null || t.isUsed() || t.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findByEmail(t.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setEnabled(true); // 👈 bạn cần field này trong User
        userRepository.save(user);

        t.setUsed(true);
        tokenRepository.save(t);

        return true;
    }

    @Override
    @Transactional
    public void saveResetPasswordToken(String email, String token) {
        Token t = Token.builder()
                .token(token)
                .email(email)
                .type("RESET")
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(t);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Token t = tokenRepository.findByToken(token)
                .orElse(null);

        if (t == null || t.isUsed() || t.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findByEmail(t.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        t.setUsed(true);
        tokenRepository.save(t);

        return true;
    }
}
