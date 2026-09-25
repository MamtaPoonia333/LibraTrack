package com.library.management.controller;

import com.library.management.dto.MessageRequest;
import java.util.Map;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
    @MessageMapping("/messages")
    @SendTo("/topic/messages")
    public Map<String, Object> send(MessageRequest request) {
        return Map.of("senderId", request.senderId(), "recipientId", request.recipientId(), "content", request.content(), "read", false);
    }
}