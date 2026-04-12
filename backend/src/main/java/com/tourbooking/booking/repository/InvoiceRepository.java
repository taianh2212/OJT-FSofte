package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.Invoice;

@Repository
public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    Invoice findByBookingId(Long bookingId);
}
