package com.tourbooking.booking.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixDatabaseComponent {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void fixDatabase() {
        log.info("Starting Database Fix for User Roles and Reporting...");
        
        try {
            // 1. Update Check Constraint for Roles
            // Drop old one if exists (it might have a different name, but the error said chk_user_role)
            try {
                jdbcTemplate.execute("ALTER TABLE Users DROP CONSTRAINT chk_user_role");
                log.info("Dropped old constraint chk_user_role");
            } catch (Exception e) {
                log.warn("Constraint chk_user_role not found or already dropped. Message: {}", e.getMessage());
            }

            // Add new constraint
            jdbcTemplate.execute("ALTER TABLE Users ADD CONSTRAINT chk_user_role CHECK (Role IN ('ADMIN', 'CUSTOMER', 'GUIDE', 'STAFF'))");
            log.info("Added new constraint with ADMIN, CUSTOMER, GUIDE, STAFF");

            // 2. Ensure bookings are SUCCESS and have dates for sample data
            jdbcTemplate.execute("UPDATE Bookings SET CreatedAt = GETDATE() WHERE CreatedAt IS NULL");
            jdbcTemplate.execute("UPDATE Bookings SET UpdatedAt = GETDATE() WHERE UpdatedAt IS NULL");
            int updated = jdbcTemplate.update("UPDATE Bookings SET Status = 'SUCCESS' WHERE Status = 'PENDING' OR Status = 'CONFIRMED' OR Status IS NULL");
            log.info("Sanitized bookings: Updated {} records to have dates and SUCCESS status.", updated);

        } catch (Exception e) {
            log.error("Failed to fix database: {}", e.getMessage());
        }
    }
}
