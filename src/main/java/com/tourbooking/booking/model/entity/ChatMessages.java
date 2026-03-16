package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChatMessages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "MessageID", nullable = false, unique = true))
public class ChatMessages extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "SenderType", length = 20)
    private String senderType;

    @Column(name = "Message", columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
