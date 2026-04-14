package com.tourbooking.booking.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.backend.model.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTourId(Long tourId);

    List<Review> findByTourIdAndRatingBetween(Long tourId, Integer minRating, Integer maxRating);

}
