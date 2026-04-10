package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.model.entity.LoyaltyPoint;

@Repository
public interface LoyaltyPointRepository
        extends JpaRepository<LoyaltyPoint, Long> {

    LoyaltyPoint findByUserId(Long userId);
}
