package com.tourbooking.booking.backend.config;

import com.tourbooking.booking.backend.model.entity.User;
import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import com.tourbooking.booking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Checking for seed data...");
            
            // Create or Update Admin
            userRepository.findByEmail("admin@gmail.com").ifPresentOrElse(
                admin -> {
                    admin.setPasswordHash(passwordEncoder.encode("123456"));
                    userRepository.save(admin);
                    log.info("Updated Admin password.");
                },
                () -> {
                    User admin = new User();
                    admin.setFullName("Admin User");
                    admin.setEmail("admin@gmail.com");
                    admin.setPasswordHash(passwordEncoder.encode("123456"));
                    admin.setRole(UserRole.ADMIN);
                    admin.setIsActive(true);
                    userRepository.save(admin);
                    log.info("Created Admin User.");
                }
            );

            // Create or Update Customer
            userRepository.findByEmail("user@gmail.com").ifPresentOrElse(
                customer -> {
                    customer.setPasswordHash(passwordEncoder.encode("123456"));
                    userRepository.save(customer);
                    log.info("Updated User password.");
                },
                () -> {
                    User customer = new User();
                    customer.setFullName("Normal User");
                    customer.setEmail("user@gmail.com");
                    customer.setPasswordHash(passwordEncoder.encode("123456"));
                    customer.setRole(UserRole.CUSTOMER);
                    customer.setIsActive(true);
                    userRepository.save(customer);
                    log.info("Created Normal User.");
                }
            );

            log.info(">>> SUCCESS: Test users are ready (admin@gmail.com / 123456)");
        };
    }
}
