package com.tourbooking.booking.service;

import com.tourbooking.booking.model.entity.LoyaltyPoint;

public interface LoyaltyService {

    LoyaltyPoint getPoint(Long userId);

    void addPoint(Long userId, int point);
}
