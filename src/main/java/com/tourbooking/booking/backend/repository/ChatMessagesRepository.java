package com.tourbooking.booking.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.backend.model.entity.ChatMessages;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
    List<ChatMessages> findByUser_IdOrderBySentAtAsc(Long userId);
    List<ChatMessages> findByUserIsNullOrderBySentAtAsc();
}
