package com.tourbooking.booking.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    USER_NOT_FOUND(1001, "User not found"),
    TOUR_NOT_FOUND(1002, "Tour not found"),
    CATEGORY_NOT_FOUND(1003, "Category not found"),
    REVIEW_NOT_FOUND(1004, "Review not found"),
    BOOKING_NOT_FOUND(1005, "Booking not found"),
    NEWSLETTER_NOT_FOUND(1006, "Newsletter subscription not found"),
    EMAIL_EXISTED(1007, "Email already exists"),
    INVALID_RATING(1008, "Rating must be between 1 and 5"),
    UNAUTHORIZED(1009, "Unauthorized access"),
    FORBIDDEN(1010, "Access denied"),
    INVALID_REQUEST(1011, "Invalid request");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
