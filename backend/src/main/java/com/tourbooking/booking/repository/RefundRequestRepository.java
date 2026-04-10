package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
}
