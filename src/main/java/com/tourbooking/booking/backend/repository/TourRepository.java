package com.tourbooking.booking.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.backend.model.entity.Tour;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    Page<Tour> findByTourNameContaining(String tourName, Pageable pageable);

    List<Tour> findByTourNameContainingIgnoreCase(String keyword);

    List<Tour> findByCategoryId(Long categoryId);

    @Query("SELECT t FROM Tour t LEFT JOIN t.schedules ts WHERE " +
           "(:keyword IS NULL OR LOWER(t.tourName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR t.rating >= :minRating) AND " +
           "(CAST(:startDate AS date) IS NULL OR ts.startDate >= :startDate)")
    List<Tour> searchToursWithFilters(@Param("keyword") String keyword, 
                                      @Param("minPrice") BigDecimal minPrice, 
                                      @Param("maxPrice") BigDecimal maxPrice, 
                                      @Param("minRating") Double minRating, 
                                      @Param("startDate") LocalDate startDate);
}
