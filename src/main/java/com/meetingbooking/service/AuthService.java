package com.meetingbooking.service;

import com.meetingbooking.dto.AuthResponse;
import com.meetingbooking.dto.LoginRequest;
import com.meetingbooking.dto.RegisterRequest;
import com.meetingbooking.entity.Role;
import com.meetingbooking.entity.User;
import com.meetingbooking.entity.VerificationToken;
import com.meetingbooking.repository.UserRepository;
import com.meetingbooking.repository.VerificationTokenRepository;
import com.meetingbooking.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис аутентификации пользователей
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Autowired
    public AuthService(UserRepository userRepository,
                      VerificationTokenRepository verificationTokenRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtService jwtService,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Регистрация нового пользователя
     */
    public User register(RegisterRequest request) {
        // Проверяем, существует ли пользователь с таким username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // По умолчанию обычный пользователь
        user.setEnabled(false); // Требуется подтверждение email
        user.setCreatedAt(LocalDateTime.now());

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Создаем токен верификации
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        verificationTokenRepository.save(verificationToken);

        // Отправляем email с подтверждением
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), token);
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем регистрацию
            System.err.println("Ошибка отправки email: " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Аутентификация пользователя
     */
    public AuthResponse login(LoginRequest request) {
        // Проверяем, существует ли пользователь
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
            request.getUsernameOrEmail(), request.getUsernameOrEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        User user = userOptional.get();

        // Проверяем, подтвержден ли email
        if (!user.getEnabled()) {
            throw new RuntimeException("Email не подтвержден. Проверьте свою почту.");
        }

        // Аутентифицируем пользователя
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(),
                request.getPassword()
            )
        );

        // Генерируем JWT токены
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getEnabled()
        );
    }

    /**
     * Подтверждение email пользователя
     */
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> tokenOptional =
            verificationTokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = tokenOptional.get();

        // Проверяем, не истек ли токен
        if (verificationToken.isExpired()) {
            return false;
        }

        // Проверяем, не использован ли токен
        if (verificationToken.isUsed()) {
            return false;
        }

        // Активируем пользователя
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Помечаем токен как использованный
        verificationToken.use();
        verificationTokenRepository.save(verificationToken);

        return true;
    }

    /**
     * Обновление access token с помощью refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        // Извлекаем username из refresh token
        String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            // Получаем пользователя
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Проверяем валидность refresh token
                if (jwtService.isTokenValid(refreshToken, UserPrincipal.create(user))) {
                    // Генерируем новые токены
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    String newAccessToken = jwtService.generateAccessToken(userPrincipal);
                    String newRefreshToken = jwtService.generateRefreshToken(userPrincipal);

                    return new AuthResponse(
                        newAccessToken,
                        newRefreshToken,
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getEnabled()
                    );
                }
            }
        }

        throw new RuntimeException("Неверный refresh token");
    }

    /**
     * Отправка повторного письма с подтверждением email
     */
    public boolean resendVerificationEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Проверяем, не подтвержден ли уже email
        if (user.getEnabled()) {
            return false;
        }

        // Удаляем старые токены
        verificationTokenRepository.deleteByUser(user);

        // Создаем новый токен
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        // Отправляем email
        try {
            emailService.sendVerificationEmail(email, token);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка отправки email: " + e.getMessage());
            return false;
        }
    }
}