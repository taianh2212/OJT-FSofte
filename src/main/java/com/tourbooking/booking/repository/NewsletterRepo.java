package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.Newsletter;

@Repository
public interface NewsletterRepo extends JpaRepository<Newsletter, Long> {
}
