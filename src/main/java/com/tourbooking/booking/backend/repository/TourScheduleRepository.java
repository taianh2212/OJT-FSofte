package com.tourbooking.booking.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.backend.model.entity.TourSchedule;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
}
