<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/entity/Newsletter.java
﻿package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Newsletter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "SubscriberID", nullable = false, unique = true))
public class Newsletter extends Base {

    @Column(name = "Email", unique = true, length = 100)
    private String email;

    @Column(name = "SubscribedAt")
    private LocalDateTime subscribedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }
}
========
package com.tourbooking.booking.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Newsletter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "SubscriberID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class Newsletter extends Base {

    @Column(name = "Email", unique = true, length = 100)
    private String email;

    @Column(name = "SubscribedAt")
    private LocalDateTime subscribedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/entity/Newsletter.java
