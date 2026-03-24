package com.example.tourbooking.booking;

import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.model.entity.enums.UserRole;
import com.tourbooking.booking.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Claims;

class BookingApplicationTests {

	@Test
	void jwtRoundTrip() {
		JwtService jwtService = new JwtService("unit-test-secret", 5);
		UserResponse user = new UserResponse();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setRole(UserRole.CUSTOMER);

		String token = jwtService.generateToken(user, "session-123");
		Claims claims = jwtService.parseClaims(token);

		org.junit.jupiter.api.Assertions.assertEquals("test@example.com", claims.getSubject());
		org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) claims.get("userId")).intValue());
		org.junit.jupiter.api.Assertions.assertEquals("CUSTOMER", claims.get("role"));
		org.junit.jupiter.api.Assertions.assertEquals("session-123", claims.get("sessionId"));
	}

}
