package com.calendarapp.controller;

import com.calendarapp.model.ChatMessage;
import com.calendarapp.rest.chat.RestChatMessage;
import com.calendarapp.rest.chat.RestCreateChatMessage;
import com.calendarapp.service.ChatService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.calendarapp.mapper.ChatMessageMapper;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final ChatMessageMapper chatMessageMapper;

    @PostMapping
    public ResponseEntity<RestChatMessage> createChatMessage(@RequestBody RestCreateChatMessage restCreateChatMessage) {
        log.info("Received chat message {}", restCreateChatMessage.getMessage());
        ChatMessage createdChatMessage = chatService.createChatMessage(restCreateChatMessage);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(chatMessageMapper.chatMessageToRestChatMessage(createdChatMessage));
    }

    @GetMapping
    public ResponseEntity<List<RestChatMessage>> getAllChatMessagesForGroup() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(chatService.getAllTasksForGroup());
    }
}
