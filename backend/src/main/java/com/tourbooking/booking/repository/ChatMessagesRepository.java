package com.tourbooking.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourbooking.booking.model.entity.ChatMessages;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
    List<ChatMessages> findByUser_IdOrderBySentAtAsc(Long userId);
    List<ChatMessages> findByGuestIdOrderBySentAtAsc(String guestId);
    List<ChatMessages> findByUserIsNullOrderBySentAtAsc();

    java.util.Optional<ChatMessages> findTopByUser_IdOrderBySentAtDesc(Long userId);
    java.util.Optional<ChatMessages> findTopByGuestIdOrderBySentAtDesc(String guestId);
    java.util.Optional<ChatMessages> findTopByUserIsNullOrderBySentAtDesc();
}
