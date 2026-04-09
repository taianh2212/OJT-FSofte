package com.tourbooking.booking.backend.service.impl;

import com.tourbooking.booking.backend.model.entity.LoyaltyPoint;
import com.tourbooking.booking.backend.repository.LoyaltyPointRepository;
import com.tourbooking.booking.backend.repository.UserRepository;
import com.tourbooking.booking.backend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Override
    public LoyaltyPoint getPoint(Long userId) {

        return loyaltyPointRepository.findByUserId(userId);
    }

    @Override
    public void addPoint(Long userId, int point) {

        LoyaltyPoint lp = loyaltyPointRepository.findByUserId(userId);

        lp.setPoints(lp.getPoints() + point);

        loyaltyPointRepository.save(lp);
    }
}