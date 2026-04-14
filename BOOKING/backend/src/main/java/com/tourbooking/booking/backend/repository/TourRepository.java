package com.tourbooking.booking.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.backend.model.entity.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    Page<Tour> findByTourNameContaining(String tourName, Pageable pageable);

    List<Tour> findByTourNameContainingIgnoreCase(String keyword);

    List<Tour> findByCategoryId(Long categoryId);

    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.schedules ts WHERE " +
           "(:keyword IS NULL OR (LOWER(t.tourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.startLocation) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
           "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR t.rating >= :minRating) AND " +
           "(:startDate IS NULL OR ts.startDate >= :startDate)")
    List<Tour> searchToursWithFilters(@Param("keyword") String keyword, 
                                      @Param("minPrice") BigDecimal minPrice, 
                                      @Param("maxPrice") BigDecimal maxPrice, 
                                      @Param("minRating") Double minRating, 
                                      @Param("startDate") LocalDate startDate);

    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.schedules ts WHERE " +
           "(:keyword IS NULL OR (" +
           "LOWER(t.tourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.startLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.itinerary) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
           "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR t.rating >= :minRating) AND " +
           "(:startDate IS NULL OR ts.startDate >= :startDate) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:transportType IS NULL OR LOWER(t.transportType) = LOWER(:transportType))")
    Page<Tour> browseTours(@Param("keyword") String keyword,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("minRating") Double minRating,
                           @Param("startDate") LocalDate startDate,
                           @Param("categoryId") Long categoryId,
                           @Param("transportType") String transportType,
                           Pageable pageable);

    // Popularity sort (booking count)
    @Query(value = """
        SELECT t.*
        FROM dbo.Tours t
        LEFT JOIN dbo.TourBookingStats s ON s.TourID = t.TourID
        WHERE
            (:keyword IS NULL OR (LOWER(t.TourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.StartLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Itinerary) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:minPrice IS NULL OR t.Price >= :minPrice)
            AND (:maxPrice IS NULL OR t.Price <= :maxPrice)
            AND (:minRating IS NULL OR t.Rating >= :minRating)
            AND (:categoryId IS NULL OR t.CategoryID = :categoryId)
            AND (:transportType IS NULL OR LOWER(t.TransportType) = LOWER(:transportType))
            AND (:startDate IS NULL OR EXISTS (
                SELECT 1 FROM dbo.TourSchedules ts
                WHERE ts.TourID = t.TourID AND ts.StartDate >= :startDate
            ))
        ORDER BY ISNULL(s.BookingCount, 0) DESC, t.TourID ASC
        OFFSET :#{#pageable.offset} ROWS FETCH NEXT :#{#pageable.pageSize} ROWS ONLY
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM dbo.Tours t
        WHERE
            (:keyword IS NULL OR (LOWER(t.TourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.StartLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Itinerary) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:minPrice IS NULL OR t.Price >= :minPrice)
            AND (:maxPrice IS NULL OR t.Price <= :maxPrice)
            AND (:minRating IS NULL OR t.Rating >= :minRating)
            AND (:categoryId IS NULL OR t.CategoryID = :categoryId)
            AND (:transportType IS NULL OR LOWER(t.TransportType) = LOWER(:transportType))
            AND (:startDate IS NULL OR EXISTS (
                SELECT 1 FROM dbo.TourSchedules ts
                WHERE ts.TourID = t.TourID AND ts.StartDate >= :startDate
            ))
        """,
        nativeQuery = true)
    Page<Tour> browseToursByPopularity(@Param("keyword") String keyword,
                                       @Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice,
                                       @Param("minRating") Double minRating,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("categoryId") Long categoryId,
                                       @Param("transportType") String transportType,
                                       Pageable pageable);

    // Distance sort (km) to a coordinate
    @Query(value = """
        SELECT t.*
        FROM dbo.Tours t
        WHERE
            t.Latitude IS NOT NULL AND t.Longitude IS NOT NULL
            AND (:keyword IS NULL OR (LOWER(t.TourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.StartLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Itinerary) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:minPrice IS NULL OR t.Price >= :minPrice)
            AND (:maxPrice IS NULL OR t.Price <= :maxPrice)
            AND (:minRating IS NULL OR t.Rating >= :minRating)
            AND (:categoryId IS NULL OR t.CategoryID = :categoryId)
            AND (:transportType IS NULL OR LOWER(t.TransportType) = LOWER(:transportType))
            AND (:startDate IS NULL OR EXISTS (
                SELECT 1 FROM dbo.TourSchedules ts
                WHERE ts.TourID = t.TourID AND ts.StartDate >= :startDate
            ))
        ORDER BY
            (6371.0 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(CAST(t.Latitude AS FLOAT)))
                * COS(RADIANS(CAST(t.Longitude AS FLOAT)) - RADIANS(:lng))
                + SIN(RADIANS(:lat)) * SIN(RADIANS(CAST(t.Latitude AS FLOAT)))
            )) ASC,
            t.TourID ASC
        OFFSET :#{#pageable.offset} ROWS FETCH NEXT :#{#pageable.pageSize} ROWS ONLY
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM dbo.Tours t
        WHERE
            t.Latitude IS NOT NULL AND t.Longitude IS NOT NULL
            AND (:keyword IS NULL OR (LOWER(t.TourName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.StartLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.Itinerary) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:minPrice IS NULL OR t.Price >= :minPrice)
            AND (:maxPrice IS NULL OR t.Price <= :maxPrice)
            AND (:minRating IS NULL OR t.Rating >= :minRating)
            AND (:categoryId IS NULL OR t.CategoryID = :categoryId)
            AND (:transportType IS NULL OR LOWER(t.TransportType) = LOWER(:transportType))
            AND (:startDate IS NULL OR EXISTS (
                SELECT 1 FROM dbo.TourSchedules ts
                WHERE ts.TourID = t.TourID AND ts.StartDate >= :startDate
            ))
        """,
        nativeQuery = true)
    Page<Tour> browseToursByDistance(@Param("keyword") String keyword,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("minRating") Double minRating,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("categoryId") Long categoryId,
                                     @Param("transportType") String transportType,
                                     @Param("lat") double lat,
                                     @Param("lng") double lng,
                                     Pageable pageable);

    boolean existsByTourNameAndStartLocation(String tourName, String startLocation);

    Optional<Tour> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    boolean existsByTourNameAndStartLocationIgnoreCase(String tourName, String startLocation);

    long countBySourceAndStartLocationIgnoreCase(String source, String startLocation);

    List<Tour> findBySourceOrderByTourName(String source);
}
