<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/dto/response/UserResponse.java
package com.tourbooking.booking.model.dto.response;

import com.tourbooking.booking.model.entity.enums.UserRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String avatarUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
========
package com.tourbooking.booking.backend.model.dto.response;

import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String avatarUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled;
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/dto/response/UserResponse.java
