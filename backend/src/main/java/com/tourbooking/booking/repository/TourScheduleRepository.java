package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.TourSchedule;
import com.tourbooking.booking.model.entity.Tour;

import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

    // UC46: Lấy tất cả schedule đang OPEN để tính lại chỗ trống
    @Query("SELECT s FROM TourSchedule s WHERE s.status = 'OPEN'")
    List<TourSchedule> findAllOpen();

    List<TourSchedule> findByGuideId(Long guideId);

    void deleteAllByTour(Tour tour);
}
