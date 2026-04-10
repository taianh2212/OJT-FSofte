package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.TourFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourFaqRepository extends JpaRepository<TourFaq, Long> {
    List<TourFaq> findByTourId(Long tourId);
}
