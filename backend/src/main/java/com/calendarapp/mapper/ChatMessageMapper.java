package com.calendarapp.mapper;

import com.calendarapp.model.ChatMessage;
import com.calendarapp.rest.chat.RestChatMessage;
import com.calendarapp.rest.chat.RestCreateChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessage restCreateChatMessageToChatMessage(RestCreateChatMessage restCreateChatMessage);

    RestChatMessage chatMessageToRestChatMessage(ChatMessage chatMessage);

    List<RestChatMessage> chatMessageListToRestChatMessageList(List<ChatMessage> chatMessages);
} 