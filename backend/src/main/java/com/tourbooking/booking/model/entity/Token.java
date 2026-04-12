package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;
import com.tourbooking.booking.model.entity.enums.TokenType;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "TokenID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class Token extends Base {

    @Column(name = "Token", nullable = false, unique = true, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "TokenType")
    private TokenType tokenType;

    @Column(name = "Expired")
    private boolean expired;

    @Column(name = "Revoked")
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", columnDefinition = "BIGINT")
    private User user;

    // Các trường phục vụ UC27-34 (Verify Email, Reset Password)
    @Column(name = "Email")
    private String email;

    @Column(name = "ExpiryDate")
    private LocalDateTime expiryDate;

    @Column(name = "Type")
    private String type;
}
