package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.entity.LoyaltyPoint;
import com.tourbooking.booking.repository.LoyaltyPointRepository;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.service.LoyaltyService;
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
