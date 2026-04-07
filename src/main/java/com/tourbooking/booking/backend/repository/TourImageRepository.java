package com.tourbooking.booking.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.backend.model.entity.TourImage;
import com.tourbooking.booking.backend.model.entity.Tour;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage, Long> {
    void deleteAllByTour(Tour tour);
}
