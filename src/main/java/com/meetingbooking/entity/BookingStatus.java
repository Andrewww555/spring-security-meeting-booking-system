package com.meetingbooking.entity;

/**
 * Статус бронирования комнаты
 */
public enum BookingStatus {
    /**
     * Активное бронирование
     */
    ACTIVE,

    /**
     * Отмененное бронирование
     */
    CANCELLED,

    /**
     * Завершенное бронирование
     */
    COMPLETED
}