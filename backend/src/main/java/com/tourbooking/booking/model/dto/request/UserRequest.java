
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
    private Boolean isActive;
}
