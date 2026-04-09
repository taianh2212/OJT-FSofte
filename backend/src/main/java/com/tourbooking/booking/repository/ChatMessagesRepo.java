package com.tourbooking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tourbooking.booking.model.entity.ChatMessages;

@Repository
public interface ChatMessagesRepo extends JpaRepository<ChatMessages, Long> {
}
