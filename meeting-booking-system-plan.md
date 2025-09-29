# План системы управления бронированием встреч

## Обзор архитектуры

Система управления бронированием встреч на базе Spring Boot 3 + Spring Security 6 + JWT

### Технологии
- **Java 17+**
- **Spring Boot 3.1+**
- **Spring Security 6**
- **Spring Data JPA**
- **PostgreSQL/MySQL**
- **JWT токены**
- **Maven**

## Структура проекта

```
meeting-booking-system/
├── src/main/java/com/booking/
│   ├── config/
│   │   ├── SecurityConfig.java          # Конфигурация безопасности
│   │   ├── JwtConfig.java               # Конфигурация JWT
│   │   └── DatabaseConfig.java          # Конфигурация БД
│   ├── controller/
│   │   ├── AuthController.java          # Аутентификация
│   │   ├── RoomController.java          # Управление комнатами
│   │   ├── BookingController.java       # Управление бронированиями
│   │   └── UserController.java          # Управление пользователями
│   ├── dto/
│   │   ├── AuthDto.java                 # DTO для аутентификации
│   │   ├── RoomDto.java                 # DTO для комнат
│   │   ├── BookingDto.java              # DTO для бронирований
│   │   └── UserDto.java                 # DTO для пользователей
│   ├── entity/
│   │   ├── User.java                    # Пользователь
│   │   ├── Role.java                    # Роль пользователя
│   │   ├── Room.java                    # Комната для встреч
│   │   ├── Booking.java                 # Бронирование
│   │   └── VerificationToken.java       # Токен верификации email
│   ├── repository/
│   │   ├── UserRepository.java          # Репозиторий пользователей
│   │   ├── RoomRepository.java          # Репозиторий комнат
│   │   ├── BookingRepository.java       # Репозиторий бронирований
│   │   └── VerificationTokenRepository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java # Фильтр JWT
│   │   ├── JwtService.java              # Сервис работы с JWT
│   │   └── CustomUserDetailsService.java # Кастомный UserDetailsService
│   ├── service/
│   │   ├── AuthService.java             # Сервис аутентификации
│   │   ├── UserService.java             # Сервис пользователей
│   │   ├── RoomService.java             # Сервис комнат
│   │   ├── BookingService.java          # Сервис бронирований
│   │   ├── EmailService.java            # Сервис отправки email
│   │   └── VerificationService.java     # Сервис верификации email
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # Глобальный обработчик исключений
│   └── MeetingBookingApplication.java   # Главный класс приложения
├── src/main/resources/
│   ├── application.yml                  # Конфигурация приложения
│   └── db/changelog/                    # Liquibase миграции
└── pom.xml                              # Зависимости Maven
```

## Модели данных

### User (Пользователь)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
```

### Role (Роль)
```java
public enum Role {
    ADMIN,      // Полный доступ
    USER,       // Обычный пользователь
    VIP_USER    // VIP пользователь с доступом к VIP комнатам
}
```

### Room (Комната)
```java
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "room_equipment")
    private List<String> equipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
```

### RoomType (Тип комнаты)
```java
public enum RoomType {
    REGULAR,    // Обычная комната
    VIP         // VIP комната
}
```

### Booking (Бронирование)
```java
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "participants_count", nullable = false)
    private Integer participantsCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
```

### BookingStatus (Статус бронирования)
```java
public enum BookingStatus {
    ACTIVE,     // Активное
    CANCELLED,  // Отменено
    COMPLETED   // Завершено
}
```

## API Endpoints

### Аутентификация
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/verify-email
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

### Комнаты
```
GET    /api/rooms              # Получить все доступные комнаты
GET    /api/rooms/{id}         # Получить комнату по ID
POST   /api/rooms              # Создать комнату (ADMIN only)
PUT    /api/rooms/{id}         # Обновить комнату (ADMIN only)
DELETE /api/rooms/{id}         # Удалить комнату (ADMIN only)
```

### Бронирования
```
GET    /api/bookings           # Получить все бронирования (ADMIN only)
GET    /api/bookings/my        # Получить мои бронирования
GET    /api/bookings/{id}      # Получить бронирование по ID
POST   /api/bookings           # Создать бронирование
PUT    /api/bookings/{id}      # Обновить бронирование
DELETE /api/bookings/{id}      # Отменить бронирование
```

### Пользователи
```
GET    /api/users              # Получить всех пользователей (ADMIN only)
GET    /api/users/{id}         # Получить пользователя по ID (ADMIN only)
PUT    /api/users/{id}         # Обновить пользователя (ADMIN only)
DELETE /api/users/{id}         # Удалить пользователя (ADMIN only)
```

## Правила доступа

### Администратор (ADMIN)
- Полный доступ ко всем эндпоинтам
- Управление пользователями
- Управление комнатами
- Просмотр всех бронирований

### Обычный пользователь (USER)
- `GET /api/rooms` - только обычные комнаты
- `GET /api/bookings/my` - только свои бронирования
- `POST /api/bookings` - создание бронирований
- `DELETE /api/bookings/{id}` - отмена своих бронирований

### VIP пользователь (VIP_USER)
- Все права обычного пользователя
- Доступ к VIP комнатам в `GET /api/rooms`
- Бронирование VIP комнат

## Конфигурация безопасности

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/rooms").hasAnyRole("USER", "VIP_USER", "ADMIN")

                // Админские эндпоинты
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/rooms/**").hasRole("ADMIN")
                .requestMatchers("/api/bookings").hasRole("ADMIN")

                // Пользовательские эндпоинты
                .requestMatchers("/api/bookings/my").hasAnyRole("USER", "VIP_USER", "ADMIN")
                .requestMatchers("/api/bookings").hasAnyRole("USER", "VIP_USER", "ADMIN")

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## JWT Конфигурация

```java
@Component
public class JwtService {
    private final String SECRET = "your-secret-key";
    private final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 минут
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 дней

    public String generateAccessToken(UserDetails userDetails) {
        // Генерация access token
    }

    public String generateRefreshToken(UserDetails userDetails) {
        // Генерация refresh token
    }

    public String extractUsername(String token) {
        // Извлечение username из токена
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Валидация токена
    }
}
```

## Следующие шаги

1. **Создать базовую структуру проекта**
2. **Настроить зависимости Maven**
3. **Создать модели данных (JPA entities)**
4. **Настроить конфигурацию безопасности**
5. **Реализовать JWT аутентификацию**
6. **Создать API контроллеры**
7. **Добавить бизнес-логику сервисов**
8. **Настроить email верификацию**
9. **Добавить валидацию и обработку ошибок**
10. **Создать тесты**

## Рекомендации по реализации

1. **Начинать с аутентификации** - это основа системы
2. **Использовать DTO** для всех API операций
3. **Валидировать входные данные** с помощью Bean Validation
4. **Обрабатывать исключения** централизованно
5. **Писать unit и integration тесты**
6. **Использовать Liquibase** для миграций БД
7. **Настраивать логирование** с SLF4J + Logback