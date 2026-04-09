package com.tourbooking.booking.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.model.entity.Tour;

@Repository
public interface TourRepo extends JpaRepository<Tour, Long> {

    Page<Tour> findByTourNameContaining(String tourName, Pageable pageable);

    List<Tour> findByTourNameContainingIgnoreCase(String keyword);

    List<Tour> findByCategoryId(Long categoryId);
}
