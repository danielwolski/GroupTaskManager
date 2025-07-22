package com.calendarapp.rest.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestChatMessage {
    private String message;
    private String username;
    private String createdAt;
}
