package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
}
