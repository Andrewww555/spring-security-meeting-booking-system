package com.meetingbooking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса входа в систему
 */
public class LoginRequest {

    @NotBlank(message = "Username или email обязателен")
    private String usernameOrEmail;

    @NotBlank(message = "Пароль обязателен")
    private String password;

    // Конструкторы
    public LoginRequest() {}

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}