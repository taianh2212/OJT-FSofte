package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.Booking;
import com.tourbooking.booking.model.entity.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByBookingDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Booking> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatus(BookingStatus status);

    // UC47: TÃƒÆ’Ã‚Â¬m booking PENDING chÃƒâ€ Ã‚Â°a cÃƒÆ’Ã‚Â³ payment thÃƒÆ’Ã‚Â nh cÃƒÆ’Ã‚Â´ng, Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ quÃƒÆ’Ã‚Â¡ hÃƒÂ¡Ã‚ÂºÃ‚Â¡n (tÃƒÂ¡Ã‚ÂºÃ‚Â¡o trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc cutoff)
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :cutoff " +
           "AND (b.payment IS NULL OR b.payment.status <> 'SUCCESS')")
    List<Booking> findPendingUnpaidBefore(@Param("cutoff") LocalDateTime cutoff);

    // UC50: Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚ÂºÃ‚Â¿m booking theo status trong thÃƒÆ’Ã‚Â¡ng
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status " +
           "AND b.createdAt >= :from AND b.createdAt < :to")
    long countByStatusAndCreatedAtBetween(@Param("status") BookingStatus status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    // UC50: TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng doanh thu CONFIRMED trong thÃƒÆ’Ã‚Â¡ng
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.createdAt >= :from AND b.createdAt < :to")
    java.math.BigDecimal sumRevenueConfirmedBetween(@Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);

    // UC50: LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ booking trong thÃƒÆ’Ã‚Â¡ng (cho bÃƒÆ’Ã‚Â¡o cÃƒÆ’Ã‚Â¡o)
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :from AND b.createdAt < :to")
    List<Booking> findAllInPeriod(@Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
