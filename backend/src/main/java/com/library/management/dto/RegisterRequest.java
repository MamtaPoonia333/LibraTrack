package com.library.management.dto;

public record RegisterRequest(String name, String email, String phoneNumber, String password) {
}