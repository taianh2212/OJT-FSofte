package com.tourbooking.booking.backend.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixDatabaseComponent implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- STARTING DATABASE INITIALIZATION ---");

        try {
            // 1. Update Tour Images & Itineraries
            seedTourData();

            // 2. Seed Admin User
            seedAdminUser();

        } catch (Exception e) {
            log.error("Initialization error (continuing app startup): {}", e.getMessage());
        }

        log.info("--- DATABASE INITIALIZATION COMPLETED ---");
    }

    private void seedTourData() {
        try {
            // Sơn Trà JSON Itinerary
            String sonTraItinerary = "[{\"title\":\"08:00 - Khởi hành\",\"content\":\"Bắt đầu tour Sơn Trà.\"},{\"title\":\"09:30 - Linh Ứng\",\"content\":\"Tham quan chùa Linh Ứng.\"},{\"title\":\"12:00 - Ăn trưa\",\"content\":\"Bữa trưa tại Sơn Trà.\"},{\"title\":\"14:00 - Kết thúc\",\"content\":\"Trở về khách sạn.\"}]";
            
            jdbcTemplate.update("UPDATE Tours SET Description = ?, Itinerary = ?, ChildPolicy = ?, WhyChooseUs = ?, IsActive = 1 WHERE TourName LIKE ?", 
                "Khám phá linh hồn của Đà Nẵng tại Bán đảo Sơn Trà.",
                sonTraItinerary,
                "Trẻ em dưới 1m miễn phí.",
                "Dịch vụ cao cấp, xe đời mới, trải nghiệm tuyệt vời.",
                "%Sơn Trà%");
                
            log.info("Seed: Sơn Trà data updated.");
        } catch (Exception e) {
            log.warn("Tour seeding warning: {}", e.getMessage());
        }
    }

    private void seedAdminUser() {
        try {
            String adminEmail = "admin@dana.com";
            // BCrypt hash cho "123456"
            String passwordHash = "$2a$10$7vj26Aptw/yE0uT/8f6BGe.1e.W0U9WfNn0/2fV9rUfB5W1N8yD9w";
            
            List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT UserID FROM Users WHERE Email = ?", adminEmail);
            
            if (users.isEmpty()) {
                jdbcTemplate.update(
                    "INSERT INTO Users (Email, FullName, PasswordHash, Role, IsActive, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    adminEmail, "DANA Admin", passwordHash, "ADMIN"
                );
                log.info("Seed: Created admin user {}", adminEmail);
            } else {
                jdbcTemplate.update("UPDATE Users SET PasswordHash = ?, IsActive = 1, Role = 'ADMIN' WHERE Email = ?", passwordHash, adminEmail);
                log.info("Seed: Updated admin user {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("Admin seeding error: {}", e.getMessage());
        }
    }
}
