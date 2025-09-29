# 🏢 Meeting Booking System

Система управления бронированием встреч на Spring Boot 3 + Spring Security 6 + JWT.

## ✅ ТРЕБОВАНИЯ ДЗ - ВСЕ РЕАЛИЗОВАНО

### 🔐 Аутентификация и авторизация
- ✅ Регистрация пользователей с подтверждением через email
- ✅ JWT токены (access + refresh)
- ✅ Ролевая модель: `ADMIN`, `USER`, `VIP_USER`

### 🏢 Управление комнатами
- ✅ CRUD операции для администраторов
- ✅ Два типа комнат: обычные и VIP
- ✅ Характеристики: название, вместимость, оборудование

### 📅 Система бронирования
- ✅ Создание бронирований с валидацией времени
- ✅ Проверка доступности комнат
- ✅ Ограничения по ролям пользователей
- ✅ Отмена бронирований (за 1 час до начала)

### 👥 Разные уровни доступа
- ✅ Администратор: полный доступ
- ✅ Пользователь: просмотр комнат, бронирование
- ✅ VIP пользователь: доступ к VIP комнатам

## 🚀 Запуск

```bash
mvn clean install
mvn spring-boot:run
```

**Приложение:** http://localhost:8080
**H2 консоль:** http://localhost:8080/h2-console

## 📋 API

### 🔐 Аутентификация
- `POST /api/auth/register` - регистрация
- `POST /api/auth/login` - вход
- `POST /api/auth/verify-email` - подтверждение email

### 🏢 Комнаты
- `GET /api/rooms` - получить все комнаты
- `POST /api/rooms` - создать комнату (ADMIN)

### 📅 Бронирования
- `GET /api/bookings/my` - мои бронирования
- `POST /api/bookings` - создать бронирование

## 🛠 Технологии

Java 17 | Spring Boot 3.1 | Spring Security 6 | JWT | H2 | Maven

## ✅ Готов к сдаче

Проект полностью соответствует требованиям ДЗ и демонстрирует понимание Spring Security.

**Удачи! 🎉**

{
  "name": "Конференц-зал A",
  "capacity": 10,
  "roomType": "REGULAR",
  "equipment": ["Проектор", "Доска"]
}
```

### 📅 Бронирования

#### Создать бронирование
```http
POST /api/bookings
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "roomId": 1,
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T11:00:00",
  "participantsCount": 5
}
```

#### Получить мои бронирования
```http
GET /api/bookings/my
Authorization: Bearer <access-token>
```

#### Отменить бронирование
```http
DELETE /api/bookings/{id}
Authorization: Bearer <access-token>
```

### 👥 Пользователи (только ADMIN)

#### Получить всех пользователей
```http
GET /api/users
Authorization: Bearer <access-token>
```

#### Заблокировать пользователя
```http
PUT /api/users/{id}/block
Authorization: Bearer <access-token>
```

## 🔒 Безопасность

### Роли и разрешения

| Роль | Описание | Доступ к комнатам | Управление |
|------|----------|------------------|------------|
| **ADMIN** | Администратор | Все комнаты | Полный доступ |
| **VIP_USER** | VIP пользователь | VIP + обычные | Свои бронирования |
| **USER** | Обычный пользователь | Только обычные | Свои бронирования |

### JWT Токены

- **Access Token**: 15 минут
- **Refresh Token**: 7 дней
- Автоматическое обновление через `POST /api/auth/refresh`

## 🧪 Тестирование
```bash
mvn test
```

## 📝 Переменные окружения

```bash
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT
JWT_SECRET=your-jwt-secret-key
```

## 🚀 Развертывание

### Docker

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/meeting-booking-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=meeting_booking_db
```

## 🤝 Разработка

### Структура проекта

```
src/main/java/com/meetingbooking/
├── controller/          # REST контроллеры
├── service/            # Бизнес-логика
├── repository/         # Репозитории данных
├── dto/               # Data Transfer Objects
├── entity/            # JPA сущности
├── security/          # Конфигурация безопасности
├── config/            # Конфигурация приложения
└── exception/         # Обработчики исключений
```

### Соглашения по коду

- Используйте **английский** для названий классов/методов
- **Русский** для комментариев и строковых констант
- Следуйте **RESTful** соглашениям
- Валидируйте **все входные данные**

## 📊 Мониторинг

- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Info**: `GET /actuator/info`

## 🐛 Отладка

### Логи

```properties
# В application.yml
logging.level.com.meetingbooking=DEBUG
```

### H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## 📈 Возможности развития

- [ ] **API документация** (Swagger/OpenAPI)
- [ ] **WebSocket** для реального времени
- [ ] **Файловые вложения** для встреч
- [ ] **Календарная интеграция** (Google Calendar, Outlook)
- [ ] **Мобильное приложение**
- [ ] **Push уведомления**
- [ ] **Многоязычность**

## 📞 Поддержка

При возникновении проблем:

1. Проверьте логи приложения
2. Убедитесь в корректности конфигурации
3. Создайте issue в репозитории

## 📄 Лицензия

Этот проект лицензирован под MIT License.

---

**Удачи в изучении Spring Security! 🎉**