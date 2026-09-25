package com.library.management.service;

import com.library.management.dto.MessageRequest;
import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(MessageRequest request) {
        Map<String, Object> payload = Map.of(
                "senderId", request.senderId(),
                "recipientId", request.recipientId(),
                "content", request.content(),
                "read", false);
        messagingTemplate.convertAndSend("/topic/messages", payload);
    }
}
