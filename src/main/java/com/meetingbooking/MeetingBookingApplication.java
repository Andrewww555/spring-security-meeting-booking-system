package com.meetingbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс приложения системы управления бронированием встреч
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MeetingBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingBookingApplication.class, args);
    }
}