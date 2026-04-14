package com.tourbooking.booking.service.impl;

import com.tourbooking.booking.model.entity.LoyaltyPoint;
import com.tourbooking.booking.model.entity.User;
import com.tourbooking.booking.repository.LoyaltyPointRepository;
import com.tourbooking.booking.repository.UserRepository;
import com.tourbooking.booking.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final UserRepository userRepository;

    @Override
    public LoyaltyPoint getPoint(Long userId) {
        LoyaltyPoint lp = loyaltyPointRepository.findByUserId(userId);
        if (lp == null) {
            // Trả về một đối tượng mặc định nếu chưa có
            lp = new LoyaltyPoint();
            lp.setPoints(0);
        }
        return lp;
    }

    @Override
    @Transactional
    public void addPoint(Long userId, int point) {
        if (point <= 0) return;

        LoyaltyPoint lp = loyaltyPointRepository.findByUserId(userId);

        if (lp == null) {
            // Tạo mới LoyaltyPoint nếu user chưa có record
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("[UC49] User {} not found, skip adding loyalty point", userId);
                return;
            }
            lp = new LoyaltyPoint();
            lp.setUser(user);
            lp.setPoints(0);
        }

        lp.setPoints(lp.getPoints() + point);
        loyaltyPointRepository.save(lp);
        log.info("[UC49] Cộng {} điểm loyalty cho user {}, tổng: {}", point, userId, lp.getPoints());
    }
}
