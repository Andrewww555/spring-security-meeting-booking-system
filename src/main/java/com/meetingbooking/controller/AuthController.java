package com.meetingbooking.controller;

import com.meetingbooking.dto.AuthResponse;
import com.meetingbooking.dto.EmailVerificationRequest;
import com.meetingbooking.dto.LoginRequest;
import com.meetingbooking.dto.RegisterRequest;
import com.meetingbooking.entity.User;
import com.meetingbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для аутентификации пользователей
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Регистрация успешна. Проверьте email для подтверждения.",
                user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                false,
                e.getMessage(),
                null
            ));
        }
    }

    /**
     * Вход в систему
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                false,
                e.getMessage(),
                null
            ));
        }
    }

    /**
     * Подтверждение email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            boolean verified = authService.verifyEmail(request.getToken());

            if (verified) {
                return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Email успешно подтвержден. Теперь вы можете войти в систему.",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Неверный или истекший токен верификации.",
                    null
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                false,
                "Ошибка верификации email: " + e.getMessage(),
                null
            ));
        }
    }

    /**
     * Обновление access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String refreshToken = extractTokenFromHeader(authHeader);
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                false,
                e.getMessage(),
                null
            ));
        }
    }

    /**
     * Повторная отправка письма подтверждения email
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        try {
            boolean sent = authService.resendVerificationEmail(email);

            if (sent) {
                return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Письмо с подтверждением отправлено повторно.",
                    null
                ));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Пользователь не найден или email уже подтвержден.",
                    null
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                false,
                "Ошибка отправки письма: " + e.getMessage(),
                null
            ));
        }
    }

    /**
     * Извлечь токен из заголовка Authorization
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Неверный формат токена");
    }

    /**
     * Внутренний класс для API ответа
     */
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}