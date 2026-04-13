package com.tourbooking.booking.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.backend.model.entity.TourSchedule;

import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    
    @Query("SELECT s FROM TourSchedule s WHERE s.status = 'OPEN'")
    List<TourSchedule> findAllOpen();

    List<TourSchedule> findByTour_Id(Long tourId);

    long countByTour_Id(Long tourId);

    List<TourSchedule> findByGuide_Id(Long guideId);
}
