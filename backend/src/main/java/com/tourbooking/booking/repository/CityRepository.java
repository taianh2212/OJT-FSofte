package com.tourbooking.booking.repository;

import com.tourbooking.booking.model.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByCityNameIgnoreCase(String cityName);

    List<City> findAllByOrderByCityName();
}
