package com.tourbooking.booking.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose directory "uploads" under "/uploads/**" URL
        Path uploadDir = Paths.get("uploads");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString());

        // Serve frontend from sibling folder while keeping old URL patterns working.
        Path frontendDir = Paths.get("..", "frontend");
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(frontendDir.resolve("assets").toUri().toString());
        registry.addResourceHandler("/pages/**")
                .addResourceLocations(frontendDir.resolve("pages").toUri().toString());
        registry.addResourceHandler("/user/**")
                .addResourceLocations(frontendDir.resolve(Paths.get("pages", "user")).toUri().toString());
        registry.addResourceHandler("/admin/**")
                .addResourceLocations(frontendDir.resolve(Paths.get("pages", "admin")).toUri().toString());
        registry.addResourceHandler("/auth/**")
                .addResourceLocations(frontendDir.resolve(Paths.get("pages", "auth")).toUri().toString());
    }
}
