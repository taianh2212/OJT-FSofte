package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByTokenAndType(String token, String type);

    void deleteByEmail(String email);
}
