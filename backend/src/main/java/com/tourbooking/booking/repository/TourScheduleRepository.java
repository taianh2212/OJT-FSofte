package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.TourSchedule;

import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    // UC46: LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ schedule Ãƒâ€žÃ¢â‚¬Ëœang OPEN Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â­nh lÃƒÂ¡Ã‚ÂºÃ‚Â¡i chÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ trÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng
    @Query("SELECT s FROM TourSchedule s WHERE s.status = 'OPEN'")
    List<TourSchedule> findAllOpen();

    List<TourSchedule> findByGuideId(Long guideId);
}
