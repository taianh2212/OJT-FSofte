package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.TourActivityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourActivityImageRepository extends JpaRepository<TourActivityImage, Long> {
    List<TourActivityImage> findByScheduleId(Long scheduleId);
}
