<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/dto/request/NewsletterRequest.java
package com.tourbooking.booking.model.dto.request;

import lombok.Data;

@Data
public class NewsletterRequest {
    private String email;
    private String fullName;
    private String subject;
    private String content;
    private String categoryName;
    private String imageUrls;
    private String highlights;
}
========
package com.tourbooking.booking.backend.model.dto.request;

import lombok.Data;

@Data
public class NewsletterRequest {
    private String email;
    private String fullName;
    private String subject;
    private String content;
    private String categoryName;
    private String imageUrls;
    private String highlights;
}
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/dto/request/NewsletterRequest.java
