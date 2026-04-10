package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.Tour;
import com.tourbooking.booking.model.entity.TourHighlight;
import java.util.List;

@Repository
public interface TourHighlightRepository extends JpaRepository<TourHighlight, Long> {
    List<TourHighlight> findByTourId(Long tourId);
    void deleteAllByTour(Tour tour);
}
