package com.tourbooking.booking.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Token extends Base {

    @Column(name = "Token", length = 500)
    private String token;

    private String email;

    private LocalDateTime expiryDate;

    private boolean used;

    private String type; // VERIFY / RESET
}