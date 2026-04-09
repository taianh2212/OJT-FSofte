<<<<<<< Updated upstream:src/main/java/com/tourbooking/booking/backend/BookingApplication.java
package com.tourbooking.booking.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
}
=======
package com.tourbooking.booking.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
}
>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/BookingApplication.java
