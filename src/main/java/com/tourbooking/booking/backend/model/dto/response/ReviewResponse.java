package com.tourbooking.booking.backend.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ReviewResponse {

    private Long reviewId;

    private String userName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

}
