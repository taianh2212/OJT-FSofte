package com.tourbooking.booking.config;

import com.tourbooking.booking.model.entity.*;
import com.tourbooking.booking.model.entity.enums.UserRole;
import com.tourbooking.booking.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DbSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        fixHighlightFontColumn();

        // 1. Seed Users (Only if not exist)
        seedUser("Quản Trị Viên", "admin@gmail.com", "123456", UserRole.ADMIN);
        seedUser("Nguyễn Văn Khách", "customer@gmail.com", "123456", UserRole.CUSTOMER);
        seedUser("Trần Văn Hướng", "guide@gmail.com", "123456", UserRole.GUIDE);

        // 2. Seed Foundation Categories
        seedCategory("Tour Trong Ngày", "Phù hợp cho lịch trình ngắn gọn, tối ưu thời gian.");
        seedCategory("Di Sản Miền Trung", "Hành trình xuyên qua các kinh thành cổ và phố cổ.");
        seedCategory("Thiên Nhiên & Biển Đảo", "Trải nghiệm biển đảo xanh mát và núi rừng hùng vĩ.");
        seedCategory("Ẩm Thực Local", "Khám phá hương vị đặc trưng của vùng miền.");

        // 3. Seed Foundation Cities
        seedCity("Đà Nẵng", 16.0544, 108.2022);
        seedCity("Hội An", 15.8801, 108.3380);
        seedCity("Huế", 16.4637, 107.5909);

        System.out.println("✅ Foundation Seeder executed successfully!");
    }

    private void fixHighlightFontColumn() {
        try {
            entityManager.createNativeQuery("ALTER TABLE TourHighlights ALTER COLUMN Highlight NVARCHAR(MAX)")
                    .executeUpdate();
        } catch (Exception e) {
            System.err.println("⚠️ Could not alter column. Skip.");
        }
    }

    private User seedUser(String fullName, String email, String password, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setIsActive(true);
            return userRepository.save(user);
        });
    }

    private Category seedCategory(String name, String desc) {
        return categoryRepository.findByCategoryNameIgnoreCase(name).orElseGet(() -> {
            Category cat = new Category();
            cat.setCategoryName(name);
            cat.setDescription(desc);
            return categoryRepository.save(cat);
        });
    }

    private City seedCity(String name, double lat, double lon) {
        return cityRepository.findByCityNameIgnoreCase(name).orElseGet(() -> {
            City city = new City();
            city.setCityName(name);
            city.setCenterLatitude(BigDecimal.valueOf(lat));
            city.setCenterLongitude(BigDecimal.valueOf(lon));
            return cityRepository.save(city);
        });
    }
}
