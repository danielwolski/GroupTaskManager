package com.calendarapp.repository;

import com.calendarapp.model.ChatMessage;
import com.calendarapp.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByGroup(Group group);
} 