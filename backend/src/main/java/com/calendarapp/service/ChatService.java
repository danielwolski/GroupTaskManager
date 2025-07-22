package com.calendarapp.service;

import com.calendarapp.mapper.ChatMessageMapper;
import com.calendarapp.model.Group;
import com.calendarapp.model.ChatMessage;
import com.calendarapp.repository.ChatMessageRepository;
import com.calendarapp.rest.chat.RestCreateChatMessage;
import com.calendarapp.rest.chat.RestChatMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    public ChatMessage createChatMessage(RestCreateChatMessage restCreateChatMessage) {
        ChatMessage chatMessage = chatMessageMapper.restCreateChatMessageToChatMessage(restCreateChatMessage);
        chatMessage.setGroup(userService.getCurrentUserGroup());
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
        log.info("Chat message saved: {}", savedChatMessage);
        return savedChatMessage;
    }

    public List<RestChatMessage> getAllTasksForGroup() {
        Group currentUserGroup = userService.getCurrentUserGroup();
        return chatMessageMapper.chatMessageListToRestChatMessageList(chatMessageRepository.findAllByGroup(currentUserGroup));
    }
}
