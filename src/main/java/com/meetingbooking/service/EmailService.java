package com.meetingbooking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки email сообщений
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@meetingbooking.com}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Отправить письмо с подтверждением email
     */
    public void sendVerificationEmail(String to, String token) {
        String subject = "Подтверждение регистрации - Meeting Booking System";
        String confirmationUrl = frontendUrl + "/verify-email?token=" + token;
        String message = String.format(
            "Здравствуйте!\n\n" +
            "Для завершения регистрации в системе Meeting Booking System, " +
            "пожалуйста, подтвердите ваш email адрес.\n\n" +
            "Перейдите по ссылке для подтверждения: %s\n\n" +
            "Ссылка действительна в течение 24 часов.\n\n" +
            "Если вы не регистрировались в системе, просто игнорируйте это письмо.\n\n" +
            "С уважением,\n" +
            "Команда Meeting Booking System",
            confirmationUrl
        );

        sendEmail(to, subject, message);
    }

    /**
     * Отправить письмо с уведомлением о создании бронирования
     */
    public void sendBookingConfirmationEmail(String to, String roomName,
                                           String startTime, String endTime) {
        String subject = "Подтверждение бронирования - Meeting Booking System";
        String message = String.format(
            "Здравствуйте!\n\n" +
            "Ваше бронирование комнаты '%s' подтверждено.\n\n" +
            "Детали бронирования:\n" +
            "Комната: %s\n" +
            "Время начала: %s\n" +
            "Время окончания: %s\n\n" +
            "Спасибо за использование нашей системы!\n\n" +
            "С уважением,\n" +
            "Команда Meeting Booking System",
            roomName, roomName, startTime, endTime
        );

        sendEmail(to, subject, message);
    }

    /**
     * Отправить письмо с уведомлением об отмене бронирования
     */
    public void sendBookingCancellationEmail(String to, String roomName,
                                           String startTime, String endTime) {
        String subject = "Отмена бронирования - Meeting Booking System";
        String message = String.format(
            "Здравствуйте!\n\n" +
            "Бронирование комнаты '%s' было отменено.\n\n" +
            "Детали отмененного бронирования:\n" +
            "Комната: %s\n" +
            "Время начала: %s\n" +
            "Время окончания: %s\n\n" +
            "Вы можете забронировать другую комнату в удобное для вас время.\n\n" +
            "С уважением,\n" +
            "Команда Meeting Booking System",
            roomName, roomName, startTime, endTime
        );

        sendEmail(to, subject, message);
    }

    /**
     * Отправить письмо с напоминанием о предстоящем бронировании
     */
    public void sendBookingReminderEmail(String to, String roomName,
                                       String startTime, String endTime) {
        String subject = "Напоминание о бронировании - Meeting Booking System";
        String message = String.format(
            "Здравствуйте!\n\n" +
            "Напоминаем о вашем предстоящем бронировании:\n\n" +
            "Комната: %s\n" +
            "Время начала: %s\n" +
            "Время окончания: %s\n\n" +
            "Ждем вас!\n\n" +
            "С уважением,\n" +
            "Команда Meeting Booking System",
            roomName, startTime, endTime
        );

        sendEmail(to, subject, message);
    }

    /**
     * Отправить письмо администратору о новой регистрации
     */
    public void sendAdminNotificationEmail(String adminEmail, String username, String email) {
        String subject = "Новый пользователь зарегистрирован - Meeting Booking System";
        String message = String.format(
            "Здравствуйте, администратор!\n\n" +
            "В системе зарегистрирован новый пользователь:\n\n" +
            "Имя пользователя: %s\n" +
            "Email: %s\n" +
            "Дата регистрации: %s\n\n" +
            "Пользователь ожидает подтверждения email адреса.\n\n" +
            "С уважением,\n" +
            "Система Meeting Booking System",
            username, email, java.time.LocalDateTime.now().toString()
        );

        sendEmail(adminEmail, subject, message);
    }

    /**
     * Отправить простое email сообщение
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем выполнение
            System.err.println("Ошибка отправки email: " + e.getMessage());
            throw new RuntimeException("Не удалось отправить email", e);
        }
    }

    /**
     * Отправить email с HTML содержимым
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            // Здесь можно добавить реализацию для HTML email
            // с использованием MimeMessage и MimeMessageHelper
            // Для простоты пока используем текстовый формат
            sendEmail(to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Ошибка отправки HTML email: " + e.getMessage());
            throw new RuntimeException("Не удалось отправить HTML email", e);
        }
    }
}