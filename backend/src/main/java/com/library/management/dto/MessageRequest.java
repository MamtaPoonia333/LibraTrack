package com.library.management.dto;

public record MessageRequest(String senderId, String recipientId, String content) {
}