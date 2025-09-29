package com.meetingbooking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса верификации email
 */
public class EmailVerificationRequest {

    @NotBlank(message = "Токен верификации обязателен")
    private String token;

    // Конструкторы
    public EmailVerificationRequest() {}

    public EmailVerificationRequest(String token) {
        this.token = token;
    }

    // Геттеры и сеттеры
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "EmailVerificationRequest{" +
                "token='[PROTECTED]'" +
                '}';
    }
}