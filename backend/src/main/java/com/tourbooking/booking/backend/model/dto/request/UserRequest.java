<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/dto/request/UserRequest.java
package com.tourbooking.booking.model.dto.request;

import com.tourbooking.booking.model.entity.enums.UserRole;
import lombok.Data;

@Data
public class UserRequest {
    private String fullName;
    private String email;
    private String password;
    private UserRole role;
    private String avatarUrl;
}
========
package com.tourbooking.booking.backend.model.dto.request;

import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import lombok.Data;

@Data
public class UserRequest {
    private String fullName;
    private String email;
    private String password;
    private UserRole role;
    private String avatarUrl;
    private Boolean isActive;
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/dto/request/UserRequest.java
