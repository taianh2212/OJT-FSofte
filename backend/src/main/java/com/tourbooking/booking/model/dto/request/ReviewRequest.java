package com.tourbooking.booking.model.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {

    private Long tourId;

    private Long userId;

    private Integer rating;

    private String comment;

}
